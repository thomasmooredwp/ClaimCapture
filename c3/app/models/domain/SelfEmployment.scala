package models.domain

import controllers.mappings.Mappings
import models.DayMonthYear
import models.yesNo.YesNoWithText

case object SelfEmployment extends Section.Identifier {
  val id = "s23"

  def isSelfEmployed(claim: Claim): Boolean = {
    claim.questionGroup[YourIncomes] match {
      case Some(employment) => employment.beenSelfEmployedSince1WeekBeforeClaim == Mappings.yes
      case _ => false
    }
  }
}

case class Employment(beenSelfEmployedSince1WeekBeforeClaim: String = "", beenEmployedSince6MonthsBeforeClaim: String = "") extends QuestionGroup(Employment)

object Employment extends QuestionGroup.Identifier {
  val id = s"${SelfEmployment.id}.g0"
}

case object SelfEmploymentDates extends QuestionGroup.Identifier {
  val id = s"${SelfEmployment.id}.g1"
}

case class SelfEmploymentDates( typeOfWork: String = "",
                                stillSelfEmployed: String = "",
                                moreThanYearAgo: String = "",
                                startThisWork: Option[DayMonthYear] = None,
                                haveAccounts: Option[String] = None,
                                knowTradingYear: Option[String] = None,
                                tradingYearStart: Option[DayMonthYear] = None,
                                paidMoney: Option[String] = None,
                                paidMoneyDate: Option[DayMonthYear] = None,
                                finishThisWork: Option[DayMonthYear] = None
                                ) extends QuestionGroup(SelfEmploymentDates)


case object SelfEmploymentPensionsAndExpenses extends QuestionGroup.Identifier {
  val id = s"${SelfEmployment.id}.g4"
}

case class SelfEmploymentPensionsAndExpenses(payPensionScheme: YesNoWithText = YesNoWithText("", None),
                                             haveExpensesForJob: YesNoWithText = YesNoWithText("", None)
                                              ) extends QuestionGroup(SelfEmploymentPensionsAndExpenses)




