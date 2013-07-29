package controllers.s2_about_you

import language.reflectiveCalls
import models.domain._
import play.api.data.Form
import play.api.data.Forms._
import controllers.Mappings._
import play.api.mvc.Controller
import models.view.CachedClaim
import utils.helpers.CarersForm._
import play.api.data.validation.Constraints._

object G2ContactDetails extends Controller with CachedClaim {
  val form = Form(
    mapping(
      call(routes.G2ContactDetails.present()),
      "address" -> address.verifying(requiredAddress),
      "postcode" -> optional(text verifying validPostcode),
      "phoneNumber" -> optional(text verifying pattern( """[0-9 \-]{1,20}""".r, "constraint.invalid", "error.invalid")),
      "mobileNumber" -> optional(text)
    )(ContactDetails.apply)(ContactDetails.unapply))

  def completedQuestionGroups(implicit claim: Claim) = claim.completedQuestionGroups(ContactDetails)

  def present = claiming { implicit claim => implicit request =>
    Ok(views.html.s2_about_you.g2_contactDetails(form.fill(ContactDetails), completedQuestionGroups))
  }

  def submit = claiming { implicit claim => implicit request =>
    form.bindEncrypted.fold(
      formWithErrors => BadRequest(views.html.s2_about_you.g2_contactDetails(formWithErrors, completedQuestionGroups)),
      contactDetails => claim.update(contactDetails) -> Redirect(routes.G3TimeOutsideUK.present()))
  }
}