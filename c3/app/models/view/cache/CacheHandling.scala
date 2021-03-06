package models.view.cache

import java.util.UUID._

import app.ConfigProperties._
import gov.dwp.carers.play2.resilientmemcached.MemcachedCacheApi
import models.domain._
import models.view.ClaimHandling
import monitoring.Histograms
import net.sf.ehcache.CacheManager
import net.spy.memcached.CASMutation
import org.joda.time.DateTime
import play.api.Logger
import play.api.Play.current
import play.api.cache.CacheApi
import play.api.mvc.{AnyContent, Request}
import utils.{RenameThread, SaveForLaterEncryption}
import scala.concurrent.duration._

import scala.concurrent.duration.Duration
import scala.util.{Success, Try, Failure}

protected trait CacheHandling extends SessionDataHandling {
  val cache = current.injector.instanceOf[CacheApi]

  def cacheKey: String

  def renameThread(uuid : String): Unit = RenameThread.renameThreadFromUuid(uuid)
  def renameThread(request : Request[AnyContent]): Unit = {
    RenameThread.renameThreadFromUuid(request.session.get(cacheKey).getOrElse(""))
  }

  def keyFrom(request: Request[AnyContent]): String = { renameThread(request); request.session.get(cacheKey).getOrElse("") }

  def claimFullKey(uuid: String): String = {
    s"${CacheHandling.claimCacheNamespace}$uuid"
  }

  def saveForLaterFullKey(uuid: String): String = {
    s"${CacheHandling.saveForLaterCacheNamespace}$uuid"
  }

  def feedbackFullKey(uuid: String): String = {
    s"${CacheHandling.feedbackCacheNamespace}$uuid"
  }

  def cookieAppVersion(request: Request[AnyContent]): String = {
    request.cookies.get(ClaimHandling.C3VERSION) match {
      case Some(cookie) => cookie.value
      case None => "N/A"
    }
  }

  /**
   * Tries to get the claim of change of circs from the cache.
    *
    * @param request the http request that has the session with uuid of claim which is the key used by cache.
   * @return None if could not find claim/CoCs. Some(claim) is could find it.
   */
  def fromCache(request: Request[AnyContent], required: Boolean = true): Option[Claim] = {
    val key = keyFrom(request)
    if (key.isEmpty) {
      if (required) {
        // Log an error if session empty or with no cacheKey entry so we know it is not a cache but a cookie issue.
        Logger.error(s"Did not receive Session information for a $cacheKey for ${request.method} url path ${request.path} and agent ${request.headers.get("User-Agent").getOrElse("Unknown agent")}. Probably a cookie issue: ${request.cookies.filterNot(_.name.startsWith("_"))}.")
      }
      None
    } else {
      Logger.info("Retrieving cache entry for request key:" + claimFullKey(key) + " with cookie application version:" + cookieAppVersion(request))
      fromCache(request, key)
    }
  }

  def fromCache(request: Request[AnyContent], key: String): Option[Claim] = {
    Logger.info("Retrieving cache entry for key:" + claimFullKey(key) + " with cookie application version:" + cookieAppVersion(request))
    getBooleanProperty("session.data.to.db") match {
      case false => cache.get[Claim](claimFullKey(key))
      case true => load(key)
    }
  }

  def saveInCache(claim: Claim) = {
    Logger.info("Saving cache entry for key:" + claimFullKey(claim.uuid))
    getBooleanProperty("session.data.to.db") match {
      case false => cache.set(claimFullKey (claim.uuid), claim, Duration(CacheHandling.expiration, SECONDS))
      case true => save(claim.uuid, claim)
    }
  }

  def removeFromCache(key: String) = {
    Logger.info("Removing cache entry for key:" + claimFullKey(key))
    getBooleanProperty("session.data.to.db") match {
      case false => cache.remove(claimFullKey(key))
      case true => delete(key)
    }
  }

  protected def recordMeasurements() = {
    Histograms.recordCacheSize(Try(CacheManager.getInstance().getCache("play").getKeysWithExpiryCheck.size()).getOrElse(0))
  }

  def secsToDate(secs: Long) = {
    new DateTime(secs * 1000)
  }

  def sflClaimExpirySecs() = {
    CacheHandling.saveForLaterCacheExpiry
  }

  def sflClaimExpiryDate() = {
    DateTime.now.plusSeconds(sflClaimExpirySecs)
  }

  def sflClaimExpiryDateSecs() = {
    sflClaimExpiryDate.getMillis / 1000
  }

  def memcacheExpirySecs() = {
    CacheHandling.saveForLaterCacheExpiry + CacheHandling.saveForLaterGracePeriod
  }

  def memcacheExpiryDate() = {
    DateTime.now.plusSeconds(memcacheExpirySecs)
  }

  def memcacheExpiryDateSecs() = {
    memcacheExpiryDate().getMillis / 1000
  }

  def feedbackClaimExpirySecs() = {
    CacheHandling.feedbackExpirySecs
  }

  def feedbackClaimExpiryDate() = {
    DateTime.now.plusSeconds(feedbackClaimExpirySecs)
  }

  def feedbackClaimExpiryDateSecs() = {
    feedbackClaimExpiryDate.getMillis / 1000
  }

  def createSaveForLaterKey(resumeSaveForLater: ResumeSaveForLater): String = {
    resumeSaveForLater.firstName.toUpperCase.replace(" ", "") + resumeSaveForLater.surname.toUpperCase.replace(" ", "") +
      resumeSaveForLater.nationalInsuranceNumber.stringify +
      resumeSaveForLater.dateOfBirth.`yyyy-MM-dd`
  }

  def createSaveForLaterKey(claim: Claim): String = {
    val yourDetails = claim.section(AboutYou).questionGroup(YourDetails).get.asInstanceOf[YourDetails]
    yourDetails.firstName.toUpperCase.replace(" ", "") + yourDetails.surname.toUpperCase.replace(" ", "") +
      yourDetails.nationalInsuranceNumber.stringify +
      yourDetails.dateOfBirth.`yyyy-MM-dd`
  }

  def decryptClaim(saveForLaterUuid: String, saveForLater: SaveForLater, resumeSaveForLater: ResumeSaveForLater): SaveForLater = {
    if (saveForLater.remainingAuthenticationAttempts > 0) {
      val key = createSaveForLaterKey(resumeSaveForLater)
      Try(SaveForLaterEncryption.decryptClaim(key, saveForLater.claim)) match {
        case Failure(e) => return setSaveForLaterRemainingAttempts(saveForLaterUuid, saveForLater)
        case Success(s) =>
          saveInCache(s)
          return updateSaveForLaterInCache(saveForLaterUuid, saveForLater, CacheHandling.saveForLaterAuthenticationAttempts, sflClaimExpiryDateSecs, memcacheExpiryDateSecs())
      }
    }
    saveForLater
  }

  def setSaveForLaterRemainingAttempts(saveForLaterUuid: String, saveForLater: SaveForLater): SaveForLater = {
    //reset status to FAILED-FINAL, save status and remove claim from cache when attempts get to 0
    //otherwise send FAILED-RETRY but not save in cache
    val remainingAttempts = saveForLater.remainingAuthenticationAttempts - 1
    remainingAttempts match {
      case 0 => updateSaveForLaterInCacheAndRemoveClaim(saveForLaterUuid, saveForLater, 0, "FAILED-FINAL")
      case _ => updateSaveForLaterInCache(saveForLaterUuid, saveForLater, remainingAttempts, saveForLater.applicationExpiry, saveForLater.cacheExpiryPeriod)
    }
  }

  def updateSaveForLaterInCache(saveForLaterUuid: String, saveForLater: SaveForLater, remainingAuthenticationAttempts: Int, applicationExpiry: Long, cacheExpiry: Long) = {
    val updatedSaveForLater = saveForLater.update(remainingAuthenticationAttempts, applicationExpiry, cacheExpiry)
    cache.set(saveForLaterFullKey(saveForLaterUuid), updatedSaveForLater, Duration(updatedSaveForLater.cacheExpiryPeriod, SECONDS))
    Logger.info(s"SFL updateSaveForLater " + saveForLaterFullKey(saveForLaterUuid) + " updated with tries left:$remainingAuthenticationAttempts claim expires:" + secsToDate(updatedSaveForLater.applicationExpiry) + " and memcache expires:" + secsToDate(updatedSaveForLater.cacheExpiryPeriod))
    if (remainingAuthenticationAttempts != CacheHandling.saveForLaterAuthenticationAttempts) updatedSaveForLater.update("FAILED-RETRY-LEFT" + remainingAuthenticationAttempts)
    else updatedSaveForLater
  }

  def updateSaveForLaterInCacheAndRemoveClaim(saveForLaterUuid: String, saveForLater: SaveForLater, remainingAuthenticationAttempts: Int, status: String) = {
    val updatedSaveForLater = saveForLater.update(status, remainingAuthenticationAttempts, null)
    cache.set(saveForLaterFullKey(saveForLaterUuid), updatedSaveForLater, Duration(updatedSaveForLater.cacheExpiryPeriod, SECONDS))
    Logger.info(s"SFL updateSaveForLater " + saveForLaterFullKey(saveForLaterUuid) + " updated with status:$updatedSaveForLater.status claim expires:" + secsToDate(updatedSaveForLater.applicationExpiry) + " and memcache expires:" + secsToDate(updatedSaveForLater.cacheExpiryPeriod))
    updatedSaveForLater
  }

  def resumeSaveForLaterFromCache(resumeSaveForLater: ResumeSaveForLater, uuid: String): Option[SaveForLater] = {
    cache.get[SaveForLater](saveForLaterFullKey(uuid)) match {
      case Some(saveForLater) =>
        if (saveForLater.status == "OK") {
          Logger.info(s"SFL resumeSaveForLater resumed " + saveForLaterFullKey(uuid) + " with status OK. Claim expiry:" + secsToDate(saveForLater.applicationExpiry) + " Cache expiry:" + secsToDate(saveForLater.cacheExpiryPeriod))
          Some(decryptClaim(uuid, saveForLater, resumeSaveForLater))
        } else {
          Logger.info(s"SFL resumeSaveForLater " + saveForLaterFullKey(uuid) + " failed resume with status ${saveForLater.status}")
          Some(saveForLater)
        }
      case _ => {
        Logger.info(s"SFL resumeSaveForLater " + saveForLaterFullKey(uuid) + " failed resume no claim found in memcache")
        None
      }
    }
  }

  def checkSaveForLaterInCache(uuid: String) = {
    cache.get[SaveForLater](saveForLaterFullKey(uuid)) match {
      case Some(saveForLater) if saveForLater.remainingAuthenticationAttempts == 2 => "FAILED-RETRY-LEFT" + saveForLater.remainingAuthenticationAttempts
      case Some(saveForLater) if saveForLater.remainingAuthenticationAttempts == 1 => "FAILED-RETRY-LEFT" + saveForLater.remainingAuthenticationAttempts
      case Some(saveForLater) => {
        Logger.info(s"SFL checkSaveForLater $uuid with status " + saveForLater.status + " Claim expiry:" + secsToDate(saveForLater.applicationExpiry) + " Cache expiry:" + secsToDate(saveForLater.cacheExpiryPeriod))
        saveForLater.status
      }
      case _ => "NO-CLAIM"
    }
  }

  def createClaimInSaveForLaterList(key: String): Unit = {
    setSaveForLaterListInCache(key, Duration(0, SECONDS))
  }

  def saveForLaterInCache(claim: Claim, path: String): Unit = {
    val key = createSaveForLaterKey(claim)
    val uuid = claim.uuid
    val saveForLater = new SaveForLater(claim = SaveForLaterEncryption.encryptClaim(claim, key), location = path,
      remainingAuthenticationAttempts = CacheHandling.saveForLaterAuthenticationAttempts, status = "OK",
      applicationExpiry = sflClaimExpiryDateSecs,
      cacheExpiryPeriod = memcacheExpiryDateSecs,
      appVersion = ClaimHandling.C3VERSION_VALUE)
    cache.set(saveForLaterFullKey(uuid), saveForLater, Duration(saveForLater.cacheExpiryPeriod, SECONDS))
    Logger.info(s"SFL save " + saveForLaterFullKey(uuid) + " saved with claim expires:" + secsToDate(saveForLater.applicationExpiry) + " and memcache expires:" + secsToDate(saveForLater.cacheExpiryPeriod))
    createClaimInSaveForLaterList(saveForLaterFullKey(uuid))
  }

  def setSaveForLaterListInCache(key: String, expiration: Duration): Unit = {
    isMemcached match {
      case true => {
        Logger.info("Using memcached CAS to store value in keylist with added key:" + key)
        val memcached = cache.asInstanceOf[MemcachedCacheApi]
        memcached.setCASList(createKeyInListMutation(key), key, CacheHandling.saveForLaterKeylist, expiration)
      }
      case _ => cache.set(CacheHandling.saveForLaterKeylist, key :: cache.get[List[String]](CacheHandling.saveForLaterKeylist).getOrElse(List[String]()).filter(_ > key), expiration)
    }
  }

  private def removeSaveForLaterClaimKeyFromList(key: String) = {
    isMemcached match {
      case true => {
        Logger.info("Using memcached CAS to remove value for uuid:"+key)
        val memcached = cache.asInstanceOf[MemcachedCacheApi]
        memcached.setCASList(removeKeyInListMutation(key), "", CacheHandling.saveForLaterKeylist, Duration(0, SECONDS))
      }
      case _ => cache.set(CacheHandling.saveForLaterKeylist, cache.get[List[String]](CacheHandling.saveForLaterKeylist).getOrElse(List[String]()).filter(_ > key), Duration(0, SECONDS))
    }
  }

  def getSaveForLaterList(): String = {
    cache.get(CacheHandling.saveForLaterKeylist).getOrElse("")
  }

  private def createKeyInListMutation(newKey: String) = {
    new CASMutation[String] {
      // This is only invoked when a value actually exists.
      def getNewValue(currentKeys: String): String = {
        val currentList: List[String] = currentKeys.split(",").map(_.trim).toList
        val newList = currentList ++ List(newKey)
        newList.distinct.mkString(",")
      }
    }
  }

  private def removeKeyInListMutation(newKey: String) = {
    new CASMutation[String] {
      // This is only invoked when a value actually exists.
      def getNewValue(currentList: String): String = {
        val c = currentList.split(",")
        val newlist = c.filter(_ > newKey)
        newlist.mkString(",")
      }
    }
  }

  def removeSaveForLaterFromCache(uuid: String): Unit = {
    cache.remove(saveForLaterFullKey(uuid))
    Logger.info(s"SFL " + saveForLaterFullKey(uuid) + " removed")
    removeSaveForLaterClaimKeyFromList(saveForLaterFullKey(uuid))
  }

  private def isMemcached: Boolean = {
    if (cache.isInstanceOf[MemcachedCacheApi]) return true
    return false
  }

  def saveFeedbackInCache(json: String): Unit = {
    val fbuuid = randomUUID.toString
    cache.set(feedbackFullKey(fbuuid), json, Duration(feedbackClaimExpiryDateSecs, SECONDS))
    Logger.info(s"FEEDBACK save " + feedbackFullKey(fbuuid) + " saved with memcache expiry:" + feedbackClaimExpiryDate)
    Logger.debug("FEEDBACK save to cache json:" + json)
    createFeedbackInList(feedbackFullKey(fbuuid))
  }

  def getFeedbackFromCache(fbuuid: String): String = {
    cache.get(fbuuid).getOrElse("")
  }

  def removeFeedbackList() = {
    cache.remove(CacheHandling.feedbackKeylist)
  }

  def getFeedbackList(): String = {
    cache.get(CacheHandling.feedbackKeylist).getOrElse("")
  }

  def createFeedbackInList(key: String): Unit = {
    setFeedbackListInCache(key, Duration(0, SECONDS))
  }

  def setFeedbackListInCache(newKey: String, expiration: Duration): Unit = {
    isMemcached match {
      case true => {
        Logger.info("Using memcached CAS to store feedback keylist with new key:" + newKey)
        val memcached = cache.asInstanceOf[MemcachedCacheApi]
        memcached.setCASList(createKeyInListMutation(newKey), newKey, CacheHandling.feedbackKeylist, expiration)
      }
      case _ => {
        Logger.info("Using EHCache to store feedback keylist with new key:" + newKey)
        val current = cache.get[String](CacheHandling.feedbackKeylist).getOrElse("")
        def appendkeys = current match {
          case "" => newKey
          case _ => current + "," + newKey
        }
        val keyList: List[String] = appendkeys.split(",").toList
        cache.set(CacheHandling.feedbackKeylist, keyList.distinct.mkString(","), expiration)
      }
    }
  }
}

object CacheHandling {
  lazy val claimCacheNamespace = "default"
  lazy val saveForLaterCacheNamespace = "SFL-"
  lazy val saveForLaterKeylist = "SFL"
  lazy val feedbackCacheNamespace = "FB-"
  lazy val feedbackKeylist = "FB"

  def expiration = getIntProperty("cache.expiry")

  def saveForLaterCacheExpiry = getIntProperty("cache.saveForLaterCacheExpirySecs")

  def saveForLaterGracePeriod = getIntProperty("cache.saveForLaterGracePeriodSecs")

  def saveForLaterAuthenticationAttempts = getIntProperty("cache.saveForLaterAuthenticationAttempts")

  def feedbackExpirySecs = getIntProperty("feedback.cache.expirysecs")
}
