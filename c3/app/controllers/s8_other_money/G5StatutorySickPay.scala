package controllers.s8_other_money

import language.reflectiveCalls
import play.api.mvc.Controller
import models.view.CachedClaim
import models.domain.{OtherStatutoryPay, Claim, StatutorySickPay}
import play.api.data.{FormError, Form}
import play.api.data.Forms._
import controllers.Mappings._
import utils.helpers.CarersForm._

object G5StatutorySickPay extends Controller with CachedClaim {
  def completedQuestionGroups(implicit claim: Claim) = claim.completedQuestionGroups(StatutorySickPay)

  val form = Form(
    mapping(
      "haveYouHadAnyStatutorySickPay" -> nonEmptyText(maxLength = sixty),
      "howMuch" -> optional(text(maxLength = sixty)),
      "howOften" -> optional(paymentFrequency verifying validPaymentFrequencyOnly),
      "employersName" -> optional(nonEmptyText(maxLength = sixty)),
      "employersAddress" -> optional(address),
      "employersPostcode" -> optional(text verifying validPostcode),
      call(routes.G5StatutorySickPay.present())
    )(StatutorySickPay.apply)(StatutorySickPay.unapply)
      .verifying("employersName.required", validateEmployerName _))

  def validateEmployerName(statutorySickPay: StatutorySickPay) = {
    statutorySickPay.haveYouHadAnyStatutorySickPay match {
      case `yes` => statutorySickPay.employersName.isDefined
      case _ => true
    }
  }

  def present = claiming { implicit claim => implicit request =>
    Ok(views.html.s8_other_money.g5_statutorySickPay(form.fill(StatutorySickPay), completedQuestionGroups))
  }

  def submit = claiming { implicit claim => implicit request =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val formWithErrorsUpdate = formWithErrors.replaceError("", "employersName.required", FormError("employersName", "error.required"))
        BadRequest(views.html.s8_other_money.g5_statutorySickPay(formWithErrorsUpdate, completedQuestionGroups))
      },
      f => claim.update(f) -> Redirect(routes.G6OtherStatutoryPay.present()))
  }
}