package controllers.your_income

import controllers.CarersForms._
import controllers.mappings.Mappings
import controllers.mappings.Mappings._
import models.domain.{Claim, DirectPayment}
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
object GDirectPayment extends Controller with CachedClaim with Navigable with I18nSupport {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]
  val form = Form(mapping(
    "stillBeingPaidThisPay_directPayment" -> carersNonEmptyText.verifying(validYesNo),
    "whenDidYouLastGetPaid" -> optional(dayMonthYear.verifying(validDate)),
    "whoPaidYouThisPay_directPayment" -> carersNonEmptyText(maxLength = Mappings.sixty),
    "amountOfThisPay" -> carersNonEmptyText(maxLength = Mappings.twelve),
    "howOftenPaidThisPay" -> carersNonEmptyText.verifying(validPaymentFrequency),
    "howOftenPaidThisPayOther" -> optional(carersNonEmptyText(maxLength = Mappings.sixty))
  )(DirectPayment.apply)(DirectPayment.unapply)
    .verifying(DirectPayment.howOftenPaidThisPayItVariesRequired)
    .verifying(DirectPayment.whenDidYouLastGetPaidRequired)
  )

  def present = claimingWithCheck { implicit claim => implicit request => implicit request2lang =>
    presentConditionally(directPayment)
  }

  def submit = claiming { implicit claim => implicit request => implicit request2lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val formWithErrorsUpdate = formWithErrors
          .replaceError("", "howOftenPaidThisPay.required", FormError("howOftenPaidThisPayOther", errorRequired))
          .replaceError("", "whenDidYouLastGetPaid.required", FormError("whenDidYouLastGetPaid", errorRequired))
        BadRequest(views.html.your_income.directPayment(formWithErrorsUpdate))
      },
      directPayment => claim.update(directPayment) -> Redirect(controllers.your_income.routes.GOtherPayments.present()))
  } withPreview()

  def directPayment(implicit claim: Claim, request: Request[AnyContent]): ClaimResult = {
    track(DirectPayment) { implicit claim => Ok(views.html.your_income.directPayment(form.fill(DirectPayment))) }
  }

  def presentConditionally(c: => ClaimResult)(implicit claim: Claim, request: Request[AnyContent]): ClaimResult = {
    if (models.domain.YourIncomeDirectPayment.visible) c
    else claim -> Redirect(controllers.your_income.routes.GOtherPayments.present())
  }
}