package controllers.s7_employment

import models.view.{Navigable, CachedClaim}
import play.api.mvc.{AnyContent, Request, Controller}
import play.api.data.Form
import play.api.data.Forms._
import models.domain.{Claim, BeenEmployed}
import utils.helpers.CarersForm._
import controllers.Mappings._
import controllers.s7_employment.Employment.jobs

object G1BeenEmployed extends Controller with CachedClaim with Navigable with EmploymentPresentation {
  val form = Form(mapping(
    "beenEmployed" -> (nonEmptyText verifying validYesNo)
  )(BeenEmployed.apply)(BeenEmployed.unapply))

  def present = claiming { implicit claim => implicit request =>
    presentConditionally(beenEmployed)
  }

  def beenEmployed(implicit claim: Claim, request: Request[AnyContent]): ClaimResult = {
    val filledForm = request.headers.get("referer") match {
      case Some(referer) if referer contains routes.G2JobDetails.present().url => form
      case Some(referer) if referer contains routes.G14JobCompletion.submit().url => form
      case _ => claim.questionGroup[BeenEmployed] match {
        case Some(b: BeenEmployed) => form.fill(b)
        case _ => form
      }
    }

    track(BeenEmployed) { implicit claim => Ok(views.html.s7_employment.g1_beenEmployed(filledForm)) }
  }

  def submit = claiming { implicit claim => implicit request =>
    import controllers.Mappings.yes

    def next(beenEmployed: BeenEmployed) = beenEmployed.beenEmployed match {
      case `yes` if jobs.size < 5 => Redirect(routes.G2JobDetails.present())
      case `yes` => Redirect(routes.G1BeenEmployed.present())
      case _ => Redirect(routes.Employment.completed())
    }

    form.bindEncrypted.fold(
      formWithErrors => BadRequest(views.html.s7_employment.g1_beenEmployed(formWithErrors)),
      beenEmployed => claim.update(beenEmployed) -> next(beenEmployed))
  }
}