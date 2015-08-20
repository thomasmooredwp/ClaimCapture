package controllers.s_disclaimer

import play.api.mvc.Controller
import models.view.{Navigable, CachedClaim}
import play.api.data.Form
import play.api.data.Forms._
import utils.helpers.CarersForm._
import controllers.CarersForms._
import models.domain.Disclaimer

object GDisclaimer extends Controller with CachedClaim with Navigable {
  val form = Form(mapping(
    "read" -> carersNonEmptyText
  )(Disclaimer.apply)(Disclaimer.unapply))

  def present = claiming { implicit claim =>  implicit request =>  lang =>
    track(Disclaimer) { implicit claim => Ok(views.html.s_disclaimer.g_disclaimer(form.fill(Disclaimer))(lang)) }
  }

  def submit = claiming { implicit claim =>  implicit request =>  lang =>
    form.bindEncrypted.fold(
      formWithErrors => BadRequest(views.html.s_disclaimer.g_disclaimer(formWithErrors)(lang)),
      disclaimer => claim.update(disclaimer) -> Redirect(controllers.s_claim_date.routes.GClaimDate.present()))
  }
}