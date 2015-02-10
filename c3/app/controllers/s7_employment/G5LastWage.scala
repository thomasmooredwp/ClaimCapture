package controllers.s7_employment

import language.reflectiveCalls
import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import models.view.{Navigable, CachedClaim}
import models.domain._
import utils.helpers.CarersForm._
import controllers.mappings.Mappings._
import Employment._
import controllers.CarersForms._
import utils.helpers.PastPresentLabelHelper._
import controllers.mappings.Mappings
import play.api.data.FormError
import models.domain.Claim


object G5LastWage extends Controller with CachedClaim with Navigable {
  def form(implicit claim: Claim) = Form(mapping(
    "iterationID" -> nonEmptyText,
    "oftenGetPaid" -> (mandatoryPaymentFrequency verifying validPaymentFrequencyOnly),
    "whenGetPaid" -> carersNonEmptyText(maxLength = Mappings.sixty),
    "lastPaidDate" -> dayMonthYear.verifying(validDate),
    "grossPay" -> required(nonEmptyText.verifying(validCurrency8Required)),
    "payInclusions" -> optional(carersText(maxLength = Mappings.threeHundred)),
    "sameAmountEachTime" -> (nonEmptyText verifying validYesNo),
    "employerOwesYouMoney" -> optional(nonEmptyText verifying validYesNo)
  )(LastWage.apply)(LastWage.unapply)
    .verifying("employerOwesYouMoney.required", validateEmployerOweMoney(claim,_))
  )

  def validateEmployerOweMoney(implicit claim: Claim, input: LastWage): Boolean = {
    claim.questionGroup(Jobs).getOrElse(Jobs()).asInstanceOf[Jobs].jobs.find(_.iterationID == input.iterationID).getOrElse(Iteration("", List())).finishedThisJob match {
      case `yes` => input.employerOwesYouMoney.isDefined
      case _ => true
    }
  }

  def present(iterationID: String) = claimingWithCheck { implicit claim =>  implicit request =>  lang =>
    track(LastWage) { implicit claim => Ok(views.html.s7_employment.g5_lastWage(form.fillWithJobID(LastWage, iterationID))(lang)) }
  }

  def submit = claimingWithCheckInIteration { iterationID => implicit claim =>  implicit request =>  lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val form = formWithErrors
          .replaceError("oftenGetPaid.frequency.other","error.maxLength",FormError("oftenGetPaid","error.maxLength"))
          .replaceError("oftenGetPaid.frequency","error.required",FormError("oftenGetPaid","error.required"))
          .replaceError("oftenGetPaid.frequency.other",errorRestrictedCharacters,FormError("oftenGetPaid",errorRestrictedCharacters))
          .replaceError("whenGetPaid", "error.required", FormError("whenGetPaid", "error.required", Seq(labelForEmployment(claim, lang, "whenGetPaid", iterationID))))
          .replaceError("whenGetPaid", errorRestrictedCharacters, FormError("whenGetPaid", errorRestrictedCharacters, Seq(labelForEmployment(claim, lang, "whenGetPaid", iterationID))))
          .replaceError("lastPaidDate", "error.required", FormError("lastPaidDate", "error.required", Seq(labelForEmployment(claim, lang, "lastPaidDate", iterationID))))
          .replaceError("grossPay", "error.required", FormError("grossPay", "error.required", Seq(labelForEmployment(claim, lang, "grossPay", iterationID))))
          .replaceError("", "employerOwesYouMoney.required", FormError("employerOwesYouMoney", "error.required", Seq(labelForEmployment(claim, lang, "employerOwesYouMoney", iterationID))))
          .replaceError("sameAmountEachTime", "error.required", FormError("sameAmountEachTime", "error.required", Seq(labelForEmployment(claim, lang, "sameAmountEachTime", iterationID))))
        BadRequest(views.html.s7_employment.g5_lastWage(form)(lang))
      },
      lastWage => claim.update(jobs.update(lastWage)) -> Redirect(routes.G8PensionAndExpenses.present(iterationID)))
  }
}