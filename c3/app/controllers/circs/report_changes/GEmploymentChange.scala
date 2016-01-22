package controllers.circs.report_changes

import play.api.Play._
import play.api.mvc.Controller
import models.view.{Navigable, CachedChangeOfCircs}
import play.api.data.Form
import play.api.data.Forms._
import models.domain._
import utils.helpers.CarersForm._
import controllers.mappings.Mappings._
import controllers.mappings.AddressMappings._
import models.yesNo._
import play.api.data.validation.{Invalid, Valid, Constraint}
import controllers.CarersForms._
import play.api.data.FormError
import play.api.data.validation.ValidationError
import play.api.mvc.Call
import scala.annotation.tailrec
import scala.collection.immutable.Stack
import scala.language.postfixOps
import play.api.i18n._


object GEmploymentChange extends Controller with CachedChangeOfCircs with Navigable with I18nSupport {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]
  val employed = "employed"
  val selfemployed = "self-employed"

  val stillCaringMapping =
    "stillCaring" -> mapping(
      "answer" -> nonEmptyText.verifying(validYesNo),
      "date" -> optional(dayMonthYear.verifying(validDate))
    )(YesNoWithDate.apply)(YesNoWithDate.unapply)
      .verifying("dateRequired", YesNoWithDate.validateNo _)

  val hasWorkFinishedYet  =
    "hasWorkFinishedYet" -> mapping(
      "answer" -> optional(nonEmptyText.verifying(validYesNo)),
      "dateWhenFinished" -> optional(dayMonthYear.verifying(validDate))
    )(OptYesNoWithDate.apply)(OptYesNoWithDate.unapply)
      .verifying("expected.yesValue", OptYesNoWithDate.validateOnYes _)

  val hasWorkStartedYet =
    "hasWorkStartedYet" -> mapping(
      "answer" -> nonEmptyText.verifying(validYesNo),
      "dateWhenStarted" -> optional(dayMonthYear.verifying(validDate)),
      "dateWhenWillItStart" -> optional(dayMonthYear.verifying(validDate))
    )(YesNoWithMutuallyExclusiveDates.apply)(YesNoWithMutuallyExclusiveDates.unapply)
      .verifying("expected.yesDateValue", YesNoWithMutuallyExclusiveDates.validateDateOnYes _)
//      .verifying("expected.yesYesNoValue", YesNoWithMutuallyExclusiveDates.validateYesNoOnYes _)
      .verifying("expected.noDateValue", YesNoWithMutuallyExclusiveDates.validateDateOnNo _)

  val typeOfWork =
    "typeOfWork" -> mapping(
      "answer" -> nonEmptyText.verifying(validTypeOfWork),
      "employerName" -> optional(carersText(minLength = 2, maxLength = 60)),
      "employerNameAndAddress" -> optional(address.verifying(requiredAddress)),
      "employerPostcode" -> optional(text verifying(restrictedPostCodeAddressStringText, validPostcode)),
      "employerContactNumber" -> optional(carersText(maxLength = 15)),
      "employerPayroll" -> optional(carersText(maxLength = 15)),
      "selfEmployedTypeOfWork" -> optional(carersText(maxLength = 35)),
      "selfEmployedTotalIncome" -> optional(carersText.verifying(validYesNoDontKnow)),
      "selfEmployedMoreAboutChanges" -> optional(carersText(maxLength = 300))
    )(YesNoWithAddressAnd2TextOrTextWithYesNoAndText.apply)(YesNoWithAddressAnd2TextOrTextWithYesNoAndText.unapply)
      .verifying("expected.employerName", YesNoWithAddressAnd2TextOrTextWithYesNoAndText.validateNameOnSpecifiedAnswer(_, "employed"))
      .verifying("expected.employerNameAndAddress1", YesNoWithAddressAnd2TextOrTextWithYesNoAndText.validateAddressOnSpecifiedAnswer(_, "employed"))
      .verifying("expected.selfEmploymentTypeOfWork", YesNoWithAddressAnd2TextOrTextWithYesNoAndText.validateText2OnSpecifiedAnswer(_, "self-employed"))
      .verifying("expected.selfEmploymentTotalIncome", YesNoWithAddressAnd2TextOrTextWithYesNoAndText.validateAnswer2OnSpecifiedAnswer(_, "self-employed"))

  val form = Form(mapping(
    stillCaringMapping,
    hasWorkStartedYet,
    hasWorkFinishedYet,
    typeOfWork
  )(CircumstancesEmploymentChange.apply)(CircumstancesEmploymentChange.unapply)
    .verifying("expected.hasWorkFinished", validHasWorkFinished _)
  )

  def present = claimingWithCheck {implicit circs => implicit request => implicit request2lang =>
    track(CircumstancesEmploymentChange) {
      implicit circs => Ok(views.html.circs.report_changes.employmentChange(form.fill(CircumstancesEmploymentChange)))
    }
  }

  def submit = claiming {implicit circs => implicit request => implicit request2lang =>
    def next(employmentChange: CircumstancesEmploymentChange):(QuestionGroup.Identifier,Call) = employmentChange.typeOfWork.answer match {
      case `employed` => {
        employmentChange.hasWorkStartedYet.answer match {
          case `yes` => {
            if (employmentChange.hasWorkFinishedYet.answer.getOrElse("no") == `yes`) CircumstancesStartedAndFinishedEmployment -> controllers.circs.report_changes.routes.GStartedAndFinishedEmployment.present()
            else CircumstancesStartedEmploymentAndOngoing -> controllers.circs.report_changes.routes.GStartedEmploymentAndOngoing.present()
          }
          case _ => CircumstancesEmploymentNotStarted -> controllers.circs.report_changes.routes.GEmploymentNotStarted.present()
        }
      }
      case _ => CircumstancesEmploymentChange -> controllers.circs.consent_and_declaration.routes.GCircsDeclaration.present()
    }

    @tailrec
    def popDeleteQG(circs:Claim,optSections:Stack[QuestionGroup.Identifier]):Claim = {
      if (optSections.isEmpty) circs
      else popDeleteQG(circs delete(optSections top),optSections pop)
    }

    form.bindEncrypted.fold(
      formWithErrors => {
        val updatedFormWithErrors = formWithErrors
          .replaceError("stillCaring","dateRequired", FormError("stillCaring.date", errorRequired))
          .replaceError("hasWorkStartedYet","expected.yesDateValue", FormError("hasWorkStartedYet.dateWhenStarted", errorRequired))
          .replaceError("hasWorkStartedYet","expected.yesYesNoValue", FormError("hasWorkFinishedYet.answer", errorRequired))
          .replaceError("hasWorkFinishedYet","expected.yesValue", FormError("hasWorkFinishedYet.dateWhenFinished", errorRequired))
          .replaceError("hasWorkStartedYet","expected.noDateValue", FormError("hasWorkStartedYet.dateWhenWillItStart", errorRequired))
          .replaceError("typeOfWork","expected.employerName", FormError("typeOfWork.employerName", errorRequired))
          .replaceError("typeOfWork","expected.employerNameAndAddress1", FormError("typeOfWork.employerNameAndAddress", errorRequired))
          .replaceError("typeOfWork","expected.employerNameAndAddress2", FormError("typeOfWork.employerNameAndAddress", "nameAndAddress.required"))
          .replaceError("typeOfWork","expected.employerPostCode", FormError("typeOfWork.employerPostcode", errorRequired))
          .replaceError("typeOfWork","expected.selfEmploymentTypeOfWork", FormError("typeOfWork.selfEmployedTypeOfWork", errorRequired))
          .replaceError("typeOfWork","expected.selfEmploymentTotalIncome", FormError("typeOfWork.selfEmployedTotalIncome", errorRequired))
          .replaceError("", "expected.hasWorkFinished", FormError("hasWorkFinishedYet.answer", errorRequired))
        BadRequest(views.html.circs.report_changes.employmentChange(updatedFormWithErrors))
      },
      employmentChange => {
        val optSections = Stack(CircumstancesStartedAndFinishedEmployment, CircumstancesStartedEmploymentAndOngoing, CircumstancesEmploymentNotStarted, CircumstancesEmploymentChange)

        val nextPage = next(employmentChange)

        val updatedCircs = popDeleteQG(circs, optSections.filter(_.id != nextPage._1.id))

        updatedCircs.update(employmentChange.copy(typeOfWork = employmentChange.typeOfWork.copy(postCode = Some(formatPostCode(employmentChange.typeOfWork.postCode.getOrElse("")))))) -> Redirect(nextPage._2)
      }
    )
  }

  def validTypeOfWork: Constraint[String] = Constraint[String]("constraint.typeOfWork") { answer =>
    answer match {
      case `employed` => Valid
      case `selfemployed` => Valid
      case _ => Invalid(ValidationError("typeOfWork.invalid"))
    }
  }

  def validHasWorkStartedYet: Constraint[String] = Constraint[String]("constraint.hasWorkStartedYet") { answer =>
    answer match {
      case `yes` => Valid
      case `no` => Valid
      case _ => Invalid(ValidationError("hasWorkStatedYet.invalid"))
    }
  }

  def validHasWorkFinished(input: CircumstancesEmploymentChange): Boolean = {
    if ((input.hasWorkStartedYet.answer == "yes") && (input.hasWorkFinishedYet.answer == None)) false
    else true
  }
}