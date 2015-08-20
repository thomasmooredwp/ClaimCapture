package controllers.s_pay_details

import controllers.CarersForms._
import controllers.mappings.Mappings._
import play.api.data.validation._

import language.reflectiveCalls
import play.api.mvc.Controller
import play.api.data.{FormError, Form}
import play.api.data.Forms._
import models.view.{Navigable, CachedClaim}
import models.domain.{BankBuildingSocietyDetails, HowWePayYou}
import utils.helpers.CarersForm._
import PayDetails._
import Constraints._

import scala.util.matching.Regex

object GHowWePayYou extends Controller with CachedClaim with Navigable {

  val bankDetailsMapping = mapping(
    "accountHolderName" -> carersNonEmptyText(maxLength = 40),
    "bankFullName" -> carersNonEmptyText(maxLength = 100),
    "sortCode" -> (sortCode verifying requiredSortCode),
    "accountNumber" -> (carersNonEmptyText(minLength = 6, maxLength = 10) verifying pattern(new Regex("\\d+"), "accountNumber", "error.number")),
    "rollOrReferenceNumber" -> carersText(maxLength = 18)
  )(BankBuildingSocietyDetails.apply)(BankBuildingSocietyDetails.unapply)

  val form = Form(mapping(
    "likeToPay" -> carersNonEmptyText(maxLength = 60),
    "paymentFrequency" -> carersNonEmptyText(maxLength = 20),
    "bankDetails" -> optional(bankDetailsMapping)
  )(HowWePayYou.apply)(HowWePayYou.unapply))


  def present = claimingWithCheck { implicit claim => implicit request => lang =>
    presentConditionally {
      track(HowWePayYou) { implicit claim => Ok(views.html.s_pay_details.g_howWePayYou(form.fill(HowWePayYou))(lang)) }
    }
  }

  def submit = claimingWithCheck { implicit claim => implicit request => lang =>
    val boundForm = form.bindEncrypted

    //retrieve the likeToPay value even if there are errors
    val likeToBePaid = boundForm.fold(formWithErrors => formWithErrors.data.getOrElse("likeToPay", ""), howWePayYou => howWePayYou.likeToBePaid)

    val bankDetailsRegex = "bankDetails\\.(.+)".r

    val handleBankDetailsField: PartialFunction[String, String] = {
      case bankDetailsRegex(field) => field
    }

    //manually validate the bank details   todo - find a better way
    //if the bank details form is visible and the user has input no data then the validation needs to be applied manually
    val newErrors: Seq[FormError] = if ((likeToBePaid == "yes") && boundForm.errors.view.map(_.key).collectFirst(handleBankDetailsField).isEmpty)
      boundForm.errors ++
        bankDetailsMapping.bind(boundForm.data.collect { case (k, v) if handleBankDetailsField.isDefinedAt(k) => (handleBankDetailsField(k), v) })
          .fold(
            fa => fa.map(f => f.copy(key=s"bankDetails.${f.key}")),
            fb => Nil)
    else
      boundForm.errors


    boundForm.copy(errors = newErrors).asInstanceOf[Form[HowWePayYou]].fold(
      formWithErrors => {
        val updatedFormWithErrors = manageErrorsSortCode(formWithErrors, "bankDetails.")
        val afterIgnoreGroupBy = ignoreGroupByForSortCode(updatedFormWithErrors, "bankDetails.")
        BadRequest(views.html.s_pay_details.g_howWePayYou(afterIgnoreGroupBy)(lang))
      },
      (howWePayYou: HowWePayYou) => {
        claim.update(howWePayYou) -> redirectPath
      })
  }

}