package services

import app.ConfigProperties._
import models.domain.Claim
import play.api.Play.current
import play.api.cache.CacheApi
import scala.concurrent.duration._

trait SubmissionCacheService {

  private val TWO_MINUTES = 120

  def checkEnabled: Boolean = {
    val check = getBooleanProperty("duplicate.submission.check")
    check
  }

  def storeInCache(claim: Claim): Unit = {
    val fingerprint = claim.getFingerprint
    val cache = current.injector.instanceOf[CacheApi]
    cache.set(fingerprint, fingerprint, Duration(getIntProperty("submission.cache.expiry"), SECONDS))
  }

  def getFromCache(claim: Claim): Option[String] = {
    val cache = current.injector.instanceOf[CacheApi]
    cache.get[String](claim.getFingerprint)
  }

  def removeFromCache(claim: Claim): Unit = {
    val cache = current.injector.instanceOf[CacheApi]
    cache.remove(claim.getFingerprint)
  }
}
