package controllers.your_income

import controllers.CarersForms._
import controllers.mappings.Mappings
import controllers.mappings.Mappings._
import models.domain.{YourIncomes, StatutoryMaternityPaternityAdoptionPay, Claim}
import models.view.ClaimHandling._
import models.view.{CachedClaim, Navigable}
import play.api.Play._
import play.api.data.Forms._
import play.api.data.{Form, FormError}
import play.api.i18n._
import play.api.mvc.{AnyContent, Controller, Request}
import utils.helpers.CarersForm._

/**
  * Created by peterwhitehead on 24/03/2016.
  */
object GStatutoryMaternityPaternityAdoptionPay extends Controller with CachedClaim with Navigable with I18nSupport {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]
  val form = Form(mapping(
    "paymentTypesForThisPay" -> carersNonEmptyText.verifying(validPaymentType),
    "stillBeingPaidThisPay_paternityMaternityAdoption" -> carersNonEmptyText.verifying(validYesNo),
    "whenDidYouLastGetPaid" -> optional(dayMonthYear.verifying(validDate)),
    "whoPaidYouThisPay_paternityMaternityAdoption" -> carersNonEmptyText(maxLength = Mappings.sixty),
    "amountOfThisPay" -> nonEmptyText.verifying(validCurrency8Required),
    "howOftenPaidThisPay" -> carersNonEmptyText.verifying(validPaymentFrequency),
    "howOftenPaidThisPayOther" -> optional(carersNonEmptyText(maxLength = Mappings.sixty))
  )(StatutoryMaternityPaternityAdoptionPay.apply)(StatutoryMaternityPaternityAdoptionPay.unapply)
    .verifying(StatutoryMaternityPaternityAdoptionPay.howOftenPaidThisPayItVariesRequired)
    .verifying(StatutoryMaternityPaternityAdoptionPay.whenDidYouLastGetPaidRequired)
  )

  def present = claimingWithCheck { implicit claim => implicit request => implicit request2lang =>
    presentConditionally(statutoryMaternityPaternityPay)
  }

  def submit = claiming { implicit claim => implicit request => implicit request2lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val formWithErrorsUpdate = formWithErrors
          .replaceError("", "howOftenPaidThisPay.required", FormError("howOftenPaidThisPayOther", errorRequired))
          .replaceError("", "whenDidYouLastGetPaid.required", FormError("whenDidYouLastGetPaid", errorRequired))
        BadRequest(views.html.your_income.statutoryMaternityPaternityAdoptionPay(formWithErrorsUpdate))
      },
      statutoryMaternityPaternityAdoptionPay => claim.update(statutoryMaternityPaternityAdoptionPay) -> Redirect(controllers.your_income.routes.GFosteringAllowance.present()))
  }.withPreviewConditionally[YourIncomes](checkGoPreview)

  def statutoryMaternityPaternityPay(implicit claim: Claim, request: Request[AnyContent]): ClaimResult = {
    track(StatutoryMaternityPaternityAdoptionPay) { implicit claim => Ok(views.html.your_income.statutoryMaternityPaternityAdoptionPay(form.fill(StatutoryMaternityPaternityAdoptionPay))) }
  }

  def presentConditionally(c: => ClaimResult)(implicit claim: Claim, request: Request[AnyContent]): ClaimResult = {
    val previousYourIncome = if (claim.navigation.beenInPreview)claim.checkYAnswers.previouslySavedClaim.get.questionGroup[YourIncomes].get else YourIncomes()
    val yourIncomes = claim.questionGroup[YourIncomes].get
    if (previousYourIncome.yourIncome_patmatadoppay != yourIncomes.yourIncome_patmatadoppay && yourIncomes.yourIncome_patmatadoppay.isDefined && models.domain.YourIncomeStatutoryMaternityPaternityAdoptionPay.visible) c
    else claim -> Redirect(controllers.your_income.routes.GFosteringAllowance.present())
  }

  private def checkGoPreview(t:(Option[YourIncomes], YourIncomes), c:(Option[Claim],Claim)): Boolean = {
    val previousEmp = t._1.get
    val currentEmp = t._2
    val fosteringAllowanceChanged = !previousEmp.yourIncome_fostering.isDefined && currentEmp.yourIncome_fostering.isDefined
    val directPaymentChanged = !previousEmp.yourIncome_directpay.isDefined && currentEmp.yourIncome_directpay.isDefined
    val otherPaymentsChanged = !previousEmp.yourIncome_anyother.isDefined && currentEmp.yourIncome_anyother.isDefined
    !(fosteringAllowanceChanged || directPaymentChanged || otherPaymentsChanged)

    //We want to go back to preview from Employment guard questions page if
    // both answers haven't changed or if one hasn't changed and the changed one is 'no' or both answers are no, or
  }
}