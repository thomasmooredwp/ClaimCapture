package controllers.your_income

import controllers.CarersForms._
import controllers.mappings.Mappings
import controllers.mappings.Mappings._
import models.domain.{YourIncomes, Claim, OtherPayments}
import models.view.ClaimHandling._
import models.view.{CachedClaim, Navigable}
import play.api.Play._
import play.api.data.Forms._
import play.api.data.{Form, FormError}
import play.api.i18n._
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Controller, Request}
import utils.helpers.CarersForm._

/**
  * Created by peterwhitehead on 24/03/2016.
  */
object GOtherPayments extends Controller with CachedClaim with Navigable with I18nSupport {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]
  val form = Form(mapping(
    "otherPaymentsInfo" -> carersNonEmptyText(maxLength = Mappings.threeThousand)
  )(OtherPayments.apply)(OtherPayments.unapply))

  def present = claimingWithCheck { implicit claim => implicit request => implicit request2lang =>
    presentConditionally(otherPayments)
  }

  def submit = claiming { implicit claim => implicit request => implicit request2lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        BadRequest(views.html.your_income.otherPayments(formWithErrors))
      },
      otherPayments => claim.update(otherPayments) -> Redirect(controllers.s_pay_details.routes.GHowWePayYou.present()))
  } withPreview()

  def otherPayments(implicit claim: Claim, request: Request[AnyContent]): ClaimResult = {
    track(OtherPayments) { implicit claim => Ok(views.html.your_income.otherPayments(form.fill(OtherPayments))) }
  }

  def presentConditionally(c: => ClaimResult)(implicit claim: Claim, request: Request[AnyContent]): ClaimResult = {
    val previousYourIncome = if (claim.navigation.beenInPreview)claim.checkYAnswers.previouslySavedClaim.get.questionGroup[YourIncomes].get else YourIncomes()
    val yourIncomes = claim.questionGroup[YourIncomes].get
    if (previousYourIncome.yourIncome_anyother != yourIncomes.yourIncome_anyother && yourIncomes.yourIncome_anyother.isDefined && models.domain.YourIncomeOtherPayments.visible) c
    else if (claim.navigation.beenInPreview)claim -> Redirect(controllers.preview.routes.Preview.present().url + getReturnToSummaryValue(claim))
    else claim -> Redirect(controllers.s_pay_details.routes.GHowWePayYou.present())
  }
}