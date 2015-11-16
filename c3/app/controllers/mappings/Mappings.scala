package controllers.mappings

import controllers.CarersForms._
import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.data.Forms._
import play.api.data.validation._
import play.api.data.{Form, FormError, Mapping}
import play.api.mvc.Request

import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

object Mappings {
  object Name {
    val maxLength = 35
  }

  val sixty = 60
  val thirtyfive = 35
  val two = 2
  val four = 4
  val five = 5
  val seventeen = 17
  val twelve = 12
  val twenty = 20
  val threeHundred = 300
  val yes = "yes"
  val no = "no"
  val dontknow = "dontknow"
  val errorRequired = "error.required"
  val required = "required"
  val errorRestrictedCharacters = "error.restricted.characters"
  val invalidNumber = "number.invalid"
  val invalidDecimal = "decimal.invalid"
  val errorInvalid = "error.invalid"
  val maxLengthError = "error.maxLength"
  val invalidYesNo: String = "yesNo.invalid"
  val invalidPostcode: String = "error.postcode"

  private val newErrorSortCode = FormError("sortCode", errorRestrictedCharacters)
  private val constraintRequired: String = "constraint.required"


  val dayMonthYear: Mapping[DayMonthYear] = mapping(
    "day" -> optional(text),
    "month" -> optional(text),
    "year" -> optional(text),
    "hour" -> optional(text),
    "minutes" -> optional(text))(DayMonthYear.convert)(DayMonthYear.extract)

  val periodFromTo: Mapping[PeriodFromTo] = mapping(
    "from" -> dayMonthYear.verifying(validDate),
    "to" -> dayMonthYear.verifying(validDate))(PeriodFromTo.apply)(PeriodFromTo.unapply)

  val paymentFrequency: Mapping[PaymentFrequency] = mapping(
    "frequency" -> text(maxLength = sixty),
    "frequency.other" -> optional(carersText(maxLength = sixty)))(PaymentFrequency.apply)(PaymentFrequency.unapply)

  val mandatoryPaymentFrequency: Mapping[PaymentFrequency] = mapping(
    "frequency" -> nonEmptyText(maxLength = sixty),
    "frequency.other" -> optional(carersText(maxLength = sixty)))(PaymentFrequency.apply)(PaymentFrequency.unapply)

  val sortCode: Mapping[SortCode] = mapping(
    "sort1" -> carersText(maxLength = two),
    "sort2" -> carersText(maxLength = two),
    "sort3" -> carersText(maxLength = two))(SortCode.apply)(SortCode.unapply)

  def required[T](mapping: Mapping[T]): Mapping[T] = {
    def required: Constraint[T] = Constraint[T](constraintRequired) { t => Valid }

    mapping.verifying(required)
  }

  def dayMonthYear(datePatterns: String*): Mapping[DayMonthYear] = mapping(
    "date" -> nonEmptyText.verifying(validDayMonthYear(datePatterns: _*)).transform(stringToDayMonthYear(datePatterns: _*), (dmy: DayMonthYear) => dmy.`dd/MM/yyyy`),
    "hour" -> optional(number(min = 0, max = 24)),
    "minutes" -> optional(number(min = 0, max = 60))
  )((dmy, h, m) => (h, m) match {
    case (Some(h1), Some(m1)) => dmy.withTime(h1, m1)
    case _ => dmy
  }
    )((dmy: DayMonthYear) => Some((dmy, dmy.hour, dmy.minutes)))

  private def validDayMonthYear(datePatterns: String*) = (date: String) => Try(stringToDayMonthYear(datePatterns: _*)(date)).isSuccess

  private def stringToDayMonthYear(datePatterns: String*) = (date: String) => {
    val datePatternDefault = "dd/MM/yyyy"

    def jodaDateTime(datePattern: String) = DateTimeFormat.forPattern(datePattern).parseDateTime(date)

    def dmy(datePattern: String) = {
      val jdt = jodaDateTime(datePattern)
      DayMonthYear(jdt.getDayOfMonth, jdt.getMonthOfYear, jdt.getYear)
    }

    def dayMonthYear(dps: List[String]): DayMonthYear = dps match {
      case Nil => dmy(datePatternDefault)
      case h :: t => Try(dmy(h)).getOrElse(dayMonthYear(t))
    }

    dayMonthYear(if (datePatterns.isEmpty) List(datePatternDefault, "dd/MM/yyyy") else datePatterns.toList)
  }


  def requiredSortCode: Constraint[SortCode] = Constraint[SortCode](constraintRequired) {
    case SortCode(s1, s2, s3) =>
      if (s1.length < 2 || s2.length < 2 || s3.length < 2) Invalid(ValidationError("error.sortcode.length"))
      else if (!(areAllDigits(s1) && areAllDigits(s2) && areAllDigits(s3))) Invalid(ValidationError("error.number"))
      else Valid
  }

  private def areAllDigits(x: String) = x forall Character.isDigit

  private def dateValidation(dmy: DayMonthYear): ValidationResult = Try(new DateTime(dmy.year.get, dmy.month.get, dmy.day.get, 0, 0)) match {
    case Success(dt: DateTime) if dt.getYear > 9999 || dt.getYear < 999 => Invalid(ValidationError(errorInvalid))
    case Success(dt: DateTime) if dt.getYear > 9999 || dt.getYear < 999 => Invalid(ValidationError(errorInvalid))
    case Success(dt: DateTime) => Valid
    case Failure(_) => Invalid(ValidationError(errorInvalid))
  }

  def validDate: Constraint[DayMonthYear] = Constraint[DayMonthYear](constraintRequired) {
      case DayMonthYear(None, None, None, _, _) => Invalid(ValidationError(errorRequired))
      case dmy@DayMonthYear(_, _, _, _, _) => dateValidation(dmy)
  }

  def validDateOnly: Constraint[DayMonthYear] = Constraint[DayMonthYear]("constraint.validateDate") { dmy =>
    dateValidation(dmy)
  }

  def validPostcode: Constraint[String] = Constraint[String]("constraint.postcode") { postcode =>
    val postcodePattern = """^(?i)(GIR 0AA)|((([A-Z][0-9][0-9]?)|(([A-Z][A-HJ-Y][0-9][0-9]?)|(([A-Z][0-9][A-Z])|([A-Z][A-HJ-Y][0-9]?[A-Z])))) ?[0-9][A-Z]{2})$""".r

    postcodePattern.pattern.matcher(postcode).matches match {
      case true => Valid
      case false => Invalid(ValidationError(invalidPostcode))
    }
  }

  def validPhoneNumber: Constraint[String] = Constraint[String]("constraint.phoneNumber") { phoneNumber =>
    validPhoneNumberPattern(phoneNumber)
  }

  /**
   * Use this method to validate phone number when it is not empty. This was created for fields which are mandatory and have to validate for
   * a valid phone number because the mandatory fields have their validation for empty fields.
   */
  def validPhoneNumberRequired: Constraint[String] = Constraint[String]("constraint.phoneNumber") { phoneNumber =>
    if (null != phoneNumber && !phoneNumber.isEmpty) validPhoneNumberPattern(phoneNumber)
    else Valid
  }

  private def validPhoneNumberPattern(phoneNumber: String) = {
    val phoneNumberPattern = """[0-9 \-]{7,20}""".r
    phoneNumberPattern.pattern.matcher(phoneNumber).matches match {
      case true => Valid
      case false => Invalid(ValidationError(errorInvalid))
    }
  }

  def validCurrency8Required: Constraint[String] = Constraint[String]("constraint.currency") { decimal =>
    val decimalPattern = """^\£?[0-9]{1,8}(\.[0-9]{1,2})?$""".r
    validCurrencyWithPattern(decimalPattern, decimal)
  }

  private def validCurrencyWithPattern(decimalPattern:Regex, decimal: String):ValidationResult = {
    if(decimal != null && !decimal.isEmpty) {
      decimalPattern.pattern.matcher(decimal).matches match {
        case true => Valid
        case false => Invalid(ValidationError(invalidDecimal))
      }
    } else {
      Valid
    }
  }

  def validDecimalNumber: Constraint[String] = Constraint[String]("constraint.decimal") { decimal =>
    val decimalPattern = """^[0-9]{1,12}(\.[0-9]{1,2})?$""".r

    decimalPattern.pattern.matcher(decimal).matches match {
      case true => Valid
      case false => Invalid(ValidationError(invalidDecimal))
    }
  }

  def validNumber: Constraint[String] = Constraint[String]("constraint.number") { number =>
    val numberPattern = """^[0-9]*$""".r

    numberPattern.pattern.matcher(number).
      matches match {
      case true => Valid
      case false => Invalid(ValidationError(invalidNumber))
    }
  }

  def validYesNo: Constraint[String] = Constraint[String]("constraint.yesNo") {
    case `yes` => Valid
    case `no` => Valid
    case _ => Invalid(ValidationError(invalidYesNo))
  }

  def validYesNoDontKnow: Constraint[String] = Constraint[String]("constraint.yesNoDontKnow") {
    case `yes` => Valid
    case `no` => Valid
    case `dontknow` => Valid
    case _ => Invalid(ValidationError(invalidYesNo))
  }

//  private def paymentFrequencyValidation(pf: PaymentFrequency): ValidationResult = Try(new PaymentFrequency(pf.frequency, pf.other)) match {
//    case Success(p: PaymentFrequency) if p.frequency == app.PensionPaymentFrequency.Other && p.other.isEmpty => Invalid(ValidationError("error.paymentFrequency"))
//    case Success(p: PaymentFrequency) => Valid
//    case Failure(_) => Invalid(ValidationError(errorInvalid))
//  }

  def validPaymentFrequencyOnly: Constraint[PaymentFrequency] = Constraint[PaymentFrequency]("constraint.validatePaymentFrequency") { pf =>
    Try(new PaymentFrequency(pf.frequency, pf.other)) match {
      case Success(p: PaymentFrequency) if p.frequency == app.PensionPaymentFrequency.Other && p.other.isEmpty => Invalid(ValidationError("error.paymentFrequency"))
      case Success(p: PaymentFrequency) => Valid
      case Failure(_) => Invalid(ValidationError(errorInvalid))
    }
  }

  def validNationality: Constraint[String] = Constraint[String]("constraint.nationality") { nationality =>
    val nationalityPattern = """[a-zA-Z \-]{1,35}""".r

    nationalityPattern.pattern.matcher(nationality).matches match {
      case true => Valid
      case false => Invalid(ValidationError("error.nationality"))
    }
  }


  def restrictedStringText: Constraint[String] = Constraint[String]("constraint.restrictedStringText") { restrictedString =>
    val restrictedStringPattern = """^[A-Za-zÀ-ƶ\s0-9\(\)&£€\"\'!\-_:;\.,/\?]*$""".r

    restrictedStringPattern.pattern.matcher(restrictedString).matches match {
      case true => Valid
      case false => Invalid(ValidationError(errorRestrictedCharacters))
    }
  }

  /**
   * Use this method to manage error codes for sort code for special characters and call this from the controller
   */
  def manageErrorsSortCode[T](formWithErrors:Form[T], prefix: String = "")(implicit request: Request[_]):Form[T] = {
    val updatedFormErrors = formWithErrors.errors.flatMap { fe =>
      if (fe.key.startsWith(s"${prefix}${newErrorSortCode.key}" )) {
        Some(newErrorSortCode.copy(key = s"$prefix${newErrorSortCode.key}"))
      } else {
        Some(fe)
      }
    }
    formWithErrors.copy(errors = updatedFormErrors)
  }

  /**
   * Use this method to display only one error message for the sort code for special characters and also to ignore
   * group by functionality for the keys as provided by the utils.helpers.CarersForm.replaceError method
   */
  def ignoreGroupByForSortCode[T](formWithErrors:Form[T], prefix: String = "")(implicit request: Request[_]):Form[T] = {
    formWithErrors.copy(errors = formWithErrors.errors.foldLeft(Seq[FormError]()) { (z: Seq[FormError], fe: FormError) =>
      if (fe.key == s"${prefix}${newErrorSortCode.key}") {
        if (!z.contains(newErrorSortCode.copy(key = s"${prefix}${newErrorSortCode.key}"))) z :+ fe else z
      } else z :+ fe
    })
  }

}
