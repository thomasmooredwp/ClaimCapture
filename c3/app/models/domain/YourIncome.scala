package models.domain

import app.{PaymentTypes, StatutoryPaymentFrequency}
import controllers.mappings.Mappings
import models.DayMonthYear
import play.api.data.validation.{ValidationError, Invalid, Valid, Constraint}
import gov.dwp.carers.xml.validation.CommonValidation
import utils.helpers.TextLengthHelper

object YourIncome extends Identifier(id = "s16") {

  val ssp = "sickpay"
  val spmp = "patmatadoppay"
  val fa = "fostering"
  val dp = "directpay"
  val rental = "rental"
  val ao = "anyother"
  val n = "none"
}

case class YourIncomes(beenEmployedSince6MonthsBeforeClaim: String = "",
                       beenSelfEmployedSince1WeekBeforeClaim: String = "",
                       yourIncome_sickpay: Option[String] = None,
                       yourIncome_patmatadoppay: Option[String] = None,
                       yourIncome_fostering: Option[String] = None,
                       yourIncome_directpay: Option[String] = None,
                       yourIncome_rentalincome: Option[String] = None,
                       yourIncome_anyother: Option[String] = None,
                       yourIncome_none: Option[String] = None
                        ) extends QuestionGroup(YourIncomes)

object YourIncomes extends QGIdentifier(id = s"${YourIncome.id}.g0") {

  def receivesStatutorySickPay(claim: Claim) = {
    claim.questionGroup[YourIncomes].getOrElse(YourIncomes()).yourIncome_sickpay.getOrElse("false") == "true"
  }

  def receivesStatutoryPay(claim: Claim) = {
    claim.questionGroup[YourIncomes].getOrElse(YourIncomes()).yourIncome_patmatadoppay.getOrElse("false") == "true"
  }
}

object YourIncomeStatutorySickPay extends Identifier(id = "s17")

object StatutorySickPay extends QGIdentifier(id = s"${YourIncomeStatutorySickPay.id}.g1") with OtherIncomes {

  def whoPaidYouMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//SickPay//WhoPaidYouThisPay//Answer")
  def amountPaidMaxLength = CommonValidation.CURRENCY_REGEX_MAX_LENGTH
  def howOftenOtherMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//SickPay//HowOftenPaidThisPayOther//Answer")
}

case class StatutorySickPay(
                             override val stillBeingPaidThisPay: String = "",
                             override val whenDidYouLastGetPaid: Option[DayMonthYear] = None,
                             override val whoPaidYouThisPay: String = "",
                             override val amountOfThisPay: String = "",
                             override val howOftenPaidThisPay: String = "",
                             override val howOftenPaidThisPayOther: Option[String] = None
                             ) extends QuestionGroup(StatutorySickPay) with OtherIncomes

object YourIncomeStatutoryMaternityPaternityAdoptionPay extends Identifier(id = "s18") {

  def whoPaidYouMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//StatutoryMaternityPaternityAdopt//WhoPaidYouThisPay//Answer")
  def amountPaidMaxLength = CommonValidation.CURRENCY_REGEX_MAX_LENGTH
  def howOftenOtherMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//StatutoryMaternityPaternityAdopt//HowOftenPaidThisPayOther//Answer")
}

object StatutoryMaternityPaternityAdoptionPay extends QGIdentifier(id = s"${YourIncomeStatutoryMaternityPaternityAdoptionPay.id}.g1") with OtherIncomes

case class StatutoryMaternityPaternityAdoptionPay(
                                                   override val paymentTypesForThisPay: String = "",
                                                   override val stillBeingPaidThisPay: String = "",
                                                   override val whenDidYouLastGetPaid: Option[DayMonthYear] = None,
                                                   override val whoPaidYouThisPay: String = "",
                                                   override val amountOfThisPay: String = "",
                                                   override val howOftenPaidThisPay: String = "",
                                                   override val howOftenPaidThisPayOther: Option[String] = None
                                                   ) extends QuestionGroup(StatutoryMaternityPaternityAdoptionPay) with OtherIncomes

object YourIncomeFosteringAllowance extends Identifier(id = "s19") {

  def whoPaidYouOtherMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//FosteringAllowance//PaymentTypesForThisPayOther//Answer")
  def whoPaidYouMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//FosteringAllowance//WhoPaidYouThisPay//Answer")
  def amountPaidMaxLength = CommonValidation.CURRENCY_REGEX_MAX_LENGTH
  def howOftenOtherMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//FosteringAllowance//HowOftenPaidThisPayOther//Answer")
}

object FosteringAllowance extends QGIdentifier( id = s"${YourIncomeFosteringAllowance.id}.g1") with OtherIncomes

case class FosteringAllowance(
                               override val paymentTypesForThisPay: String = "",
                               override val paymentTypesForThisPayOther: Option[String] = None,
                               override val stillBeingPaidThisPay: String = "",
                               override val whenDidYouLastGetPaid: Option[DayMonthYear] = None,
                               override val whoPaidYouThisPay: String = "",
                               override val amountOfThisPay: String = "",
                               override val howOftenPaidThisPay: String = "",
                               override val howOftenPaidThisPayOther: Option[String] = None
                               ) extends QuestionGroup(FosteringAllowance) with OtherIncomes

object YourIncomeDirectPayment extends Identifier(id = "s20") {

  def whoPaidYouMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//DirectPay//WhoPaidYouThisPay//Answer")
  def amountPaidMaxLength = CommonValidation.CURRENCY_REGEX_MAX_LENGTH
  def howOftenOtherMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//DirectPay//HowOftenPaidThisPayOther//Answer")
}

object DirectPayment extends QGIdentifier(id = s"${YourIncomeDirectPayment.id}.g1") with OtherIncomes

case class DirectPayment(
                          override val stillBeingPaidThisPay: String = "",
                          override val whenDidYouLastGetPaid: Option[DayMonthYear] = None,
                          override val whoPaidYouThisPay: String = "",
                          override val amountOfThisPay: String = "",
                          override val howOftenPaidThisPay: String = "",
                          override val howOftenPaidThisPayOther: Option[String] = None
                          ) extends QuestionGroup(DirectPayment) with OtherIncomes

object YourIncomeRentalIncome extends Identifier(id = "s24") {

  def rentalIncomeMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//RentalIncomeInfo//Answer")
}

object RentalIncome extends QGIdentifier(id = s"${YourIncomeRentalIncome.id}.g1")

case class RentalIncome(
                         rentalIncomeInfo: String = ""
                         ) extends QuestionGroup(RentalIncome)


object YourIncomeOtherPayments extends Identifier(id = "s21") {
  def otherPaymentsMaxLength = TextLengthHelper.textMaxLength("DWPCAClaim//Incomes//OtherPaymentsInfo//Answer")
}

object OtherPayments extends QGIdentifier(id = s"${YourIncomeOtherPayments.id}.g1")

case class OtherPayments(
                          otherPaymentsInfo: String = ""
                          ) extends QuestionGroup(OtherPayments)


trait OtherIncomes {
  val paymentTypesForThisPay: String = ""
  val paymentTypesForThisPayOther: Option[String] = None
  val stillBeingPaidThisPay: String = ""
  val whenDidYouLastGetPaid: Option[DayMonthYear] = None
  val whoPaidYouThisPay: String = ""
  val amountOfThisPay: String = ""
  val howOftenPaidThisPay: String = ""
  val howOftenPaidThisPayOther: Option[String] = None

  def howOftenPaidThisPayItVariesRequired: Constraint[OtherIncomes] = Constraint[OtherIncomes]("constraint.howOftenPaidThisPay") {
    income =>
      if (income.howOftenPaidThisPay == StatutoryPaymentFrequency.ItVaries) {
        income.howOftenPaidThisPayOther match {
          case Some(howOften) => Valid
          case _ => Invalid(ValidationError("howOftenPaidThisPay.required"))
        }
      }
      else Valid
  }

  def whenDidYouLastGetPaidRequired: Constraint[OtherIncomes] = Constraint[OtherIncomes]("constraint.whenDidYouLastGetPaid") {
    income =>
      if (income.stillBeingPaidThisPay == Mappings.no) {
        income.whenDidYouLastGetPaid match {
          case Some(whenLastPaid) => Valid
          case _ => Invalid(ValidationError("whenDidYouLastGetPaid.required"))
        }
      }
      else Valid
  }

  def paymentTypesForThisPayOtherRequired: Constraint[OtherIncomes] = Constraint[OtherIncomes]("constraint.paymentTypesForThisPay") {
    income =>
      if (income.paymentTypesForThisPay == PaymentTypes.Other) {
        income.paymentTypesForThisPayOther match {
          case Some(howOften) => Valid
          case _ => Invalid(ValidationError("paymentTypesForThisPayOther.required"))
        }
      }
      else Valid
  }
}
