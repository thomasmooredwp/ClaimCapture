package controllers.save_for_later

import models.domain._
import models.view.{CachedClaim, Navigable}
import play.api.Play._
import play.api.data.{Form}
import play.api.data.Forms._
import play.api.i18n._
import play.api.mvc._
import scala.language.reflectiveCalls
import controllers.CarersForms._
import controllers.mappings.Mappings._
import controllers.mappings.NINOMappings._
import utils.helpers.CarersForm._
import play.api.{Logger}


object GResume extends Controller with CachedClaim with Navigable with I18nSupport {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]

  val form = Form(mapping(
    "firstName" -> carersNonEmptyText(maxLength = 17),
    "surname" -> carersNonEmptyText(maxLength = Name.maxLength),
    "nationalInsuranceNumber" -> nino.verifying(filledInNino, validNino),
    "dateOfBirth" -> dayMonthYear.verifying(validDate),
    "uuid" -> text
  )(ResumeSaveForLater.apply)(ResumeSaveForLater.unapply))

  def present = newClaim { implicit claim => implicit request => implicit lang =>
    var savekeyuuid = createParamsMap(request.queryString).getOrElse("savekey", "")
    if (savekeyuuid.equals("")) {
      Logger.debug("Error - no savekey parameter passed to resume")
      Ok(views.html.save_for_later.resumeNotExist(lang))
    }
    else {
      val f2 = form.fill(ResumeSaveForLater(uuid = savekeyuuid))

      // using an intermediate variable for status to allow easier testing to amend the status in debug
      val status = checkSaveForLaterInCache(savekeyuuid)
      status match {
        case "OK" => Ok(views.html.save_for_later.resumeClaim(f2))
        case "NO-CLAIM" => Ok(views.html.save_for_later.resumeNotExist(lang))
        case "FAILED-FINAL" => Ok(views.html.save_for_later.resumeFailedFinal(lang))
        case "EXPIRED" => Ok(views.html.save_for_later.resumeExpired(lang))
        case s => {
          BadRequest(views.html.save_for_later.resumeClaim(form.withError("surname", "Failure retrieving claim bad status:" + s)))
        }
      }
    }
  }

  def submit = newClaim { implicit claim => implicit request => implicit lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        BadRequest(views.html.save_for_later.resumeClaim(formWithErrors))
      },
      resumeSaveForLater => {
        val retrievedClaim = resumeSaveForLaterFromCache(resumeSaveForLater, resumeSaveForLater.uuid)
        retrievedClaim match {
          case Some(sfl) if sfl.status.equals("OK") => claim -> Redirect(routes.GResume.resume() + "?uuid=" + resumeSaveForLater.uuid)
          case Some(sfl) if sfl.status.equals("FAILED-RETRY") && sfl.remainingAuthenticationAttempts == 2 => BadRequest(views.html.save_for_later.resumeClaim(form.fill(resumeSaveForLater).withGlobalError("Your details don't match this application. You have 2 attempts left.")))
          case Some(sfl) if sfl.status.equals("FAILED-RETRY") && sfl.remainingAuthenticationAttempts == 1 => BadRequest(views.html.save_for_later.resumeClaim(form.fill(resumeSaveForLater).withGlobalError("Your details don't match this application.You have 1 attempt left.")))
          case Some(sfl) if sfl.status.equals("FAILED-FINAL") => Ok(views.html.save_for_later.resumeFailedFinal(lang))
          case _ => Ok(views.html.save_for_later.resumeClaim(form))
        }
      })
  }

  def resume = claimingWithResume { implicit claim => implicit request => implicit lang =>
    Logger.debug("resumeClaim got claim uuid:" + claim.uuid + " and redirecting to:" + claim.navigation.current.toString())
    claim -> Redirect(claim.navigation.current.toString())
  }
}
