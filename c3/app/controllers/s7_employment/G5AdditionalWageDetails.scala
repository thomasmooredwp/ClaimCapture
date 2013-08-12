package controllers.s7_employment

import scala.language.reflectiveCalls
import models.view.CachedClaim
import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import models.domain.AdditionalWageDetails
import utils.helpers.CarersForm._
import controllers.Mappings._
import Employment._
import play.api.data.validation.{Valid, Constraint}
import utils.helpers.PastPresentLabelHelper._
import play.api.data.FormError

object G5AdditionalWageDetails extends Controller with CachedClaim {
  val form = Form(
    mapping(
      "jobID" -> nonEmptyText,
      "oftenGetPaid" -> optional(paymentFrequency),
      "whenGetPaid" -> optional(text),
      "holidaySickPay" -> optional(text verifying validYesNo),
      "anyOtherMoney" -> (nonEmptyText verifying validYesNo),
      "otherMoney" -> optional(text verifying Constraint[String]("constraint.required") { s => Valid }),
      "employerOwesYouMoney" -> (nonEmptyText verifying validYesNo)
    )(AdditionalWageDetails.apply)(AdditionalWageDetails.unapply)
    .verifying("otherMoney", AdditionalWageDetails.validateOtherMoney _))

  def present(jobID: String) = claiming { implicit claim => implicit request =>
    dispatch(Ok(views.html.s7_employment.g5_additionalWageDetails(form.fillWithJobID(AdditionalWageDetails, jobID), completedQuestionGroups(AdditionalWageDetails, jobID))))
  }

  def submit = claimingInJob { jobID => implicit claim => implicit request =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val formWithErrorsUpdate = formWithErrors
          .replaceError("anyOtherMoney", "error.required", FormError("anyOtherMoney", "error.required", Seq(pastPresentLabelForEmployment(claim, didYou, doYou , jobID))))
        dispatch(BadRequest(views.html.s7_employment.g5_additionalWageDetails(formWithErrorsUpdate, completedQuestionGroups(AdditionalWageDetails, jobID))))
      },
      wageDetails => claim.update(jobs.update(wageDetails)) -> Redirect(routes.G6MoneyOwedbyEmployer.present(jobID)))
  }
}