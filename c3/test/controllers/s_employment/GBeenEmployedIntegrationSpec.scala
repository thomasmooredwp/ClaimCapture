package controllers.s_employment

import controllers.ClaimScenarioFactory
import org.specs2.mutable._
import play.api.test.FakeApplication
import controllers.ClaimScenarioFactory._
import utils.pageobjects.your_income.{GStatutorySickPayPage, GYourIncomePage}
import utils.{WithJsBrowser, WithBrowser}
import utils.pageobjects._
import utils.pageobjects.s_education.GYourCourseDetailsPage
import utils.pageobjects.s_employment._
import utils.pageobjects.s_self_employment.{GSelfEmploymentDatesPage}
import utils.pageobjects.s_claim_date.{GClaimDatePage, GClaimDatePageContext}


class GBeenEmployedIntegrationSpec extends Specification {
  section("integration", models.domain.Employed.id)
  "Been Employed" should {
    "present, having indicated that the carer has been employed" in new WithBrowser with PageObjects {
      val claimDate = new GClaimDatePage(context) goToThePage()
      claimDate.fillPageWith(s7SelfEmployedAndEmployed())
      claimDate.submitPage()

      override implicit def implicitApp: FakeApplication = super.implicitApp

      val employment = new GYourIncomePage(claimDate.ctx) goToThePage()

      employment must beAnInstanceOf[GYourIncomePage]
    }

    """be bypassed and go onto "statutory sick pay" having indicated that "employment" is not required.""" in new WithBrowser with PageObjects {
      val claimDate = new GClaimDatePage(context) goToThePage()
      claimDate.fillPageWith(s7SSPOnly())
      claimDate.submitPage()

      val employment = new GYourIncomePage(claimDate.ctx) goToThePage()
      employment.fillPageWith(s7SSPOnly())
      val statutorySickPay = employment.submitPage()

      statutorySickPay must beAnInstanceOf[GStatutorySickPayPage]
    }


    """progress to next section i.e. "self employed".""" in new WithBrowser with PageObjects {
      val claimDate = new GClaimDatePage(context) goToThePage()
      claimDate.fillPageWith(s7SelfEmployedAndEmployed())
      claimDate.submitPage()

      val employment = new GYourIncomePage(claimDate.ctx) goToThePage()
      employment.fillPageWith(s7SelfEmployedAndEmployed())
      val jobDetails = employment.submitPage()

      jobDetails must beAnInstanceOf[GJobDetailsPage]
    }

    "start employment entry" in new WithBrowser with PageObjects {
      val claimDate = new GClaimDatePage(context) goToThePage()
      claimDate.fillPageWith(s7EmployedNotSelfEmployed())
      claimDate.submitPage()

      val employment = new GYourIncomePage(claimDate.ctx) goToThePage()
      employment.fillPageWith(s7EmployedNotSelfEmployed())
      val jobDetails = employment.submitPage()

      jobDetails must beAnInstanceOf[GJobDetailsPage]
    }

    "show 1 error upon submitting no mandatory data" in new WithBrowser with PageObjects with EmployedHistoryPage {
      val historyPage = goToHistoryPage(ClaimScenarioFactory.s7EmploymentMinimal())
      historyPage must beAnInstanceOf[GBeenEmployedPage]
      historyPage submitPage()

      historyPage.listErrors.size shouldEqual 1
    }

    """go back to "education".""" in new WithBrowser with PageObjects {
      val claimDate = new GClaimDatePage(context) goToThePage()
      claimDate.fillPageWith(s7EmployedNotSelfEmployed())
      claimDate.submitPage()

      val education = new GYourCourseDetailsPage(claimDate.ctx) goToThePage()
      val employment = new GYourIncomePage(education.ctx) goToThePage()
      employment.goBack() must beAnInstanceOf[GYourCourseDetailsPage]
    }

    """remember been employed question""" in new WithBrowser with EmployedHistoryPage {
      val employmentData = ClaimScenarioFactory.s7EmploymentMinimal()
      var historyPage = goToHistoryPage(ClaimScenarioFactory.s7EmploymentMinimal())
      historyPage must beAnInstanceOf[GBeenEmployedPage]
      employmentData.EmploymentHaveYouBeenEmployedAtAnyTime_1 = "No"
      historyPage fillPageWith employmentData
      val nextPage = historyPage submitPage()

      nextPage must beAnInstanceOf[GEmploymentAdditionalInfoPage]
      historyPage = nextPage goBack()
      historyPage.readYesNo("#beenEmployed") mustNotEqual None
    }

    """have job data after filling a job""" in new WithBrowser with EmployedHistoryPage {
      val employmentData = ClaimScenarioFactory.s7EmploymentMinimal()
      var historyPage = goToHistoryPage(ClaimScenarioFactory.s7EmploymentMinimal())
      historyPage must beAnInstanceOf[GBeenEmployedPage]
      historyPage.source must contain("Tesco's")
      historyPage.source must contain("01/01/2013")
    }

    """Display start date with text "Before" when start date is before claim date""" in new WithBrowser with EmployedHistoryPage {
      var historyPage = goToHistoryPage(ClaimScenarioFactory.s7EmploymentBeforeClamDateYes())
      historyPage must beAnInstanceOf[GBeenEmployedPage]
      historyPage.source must contain("Before 10/09/2016") // This is one month before claim date
    }

    "not have warning on page when less than maximum jobs" in new WithJsBrowser with PageObjects {
      val employmentData = GClaimDatePage(context) goToThePage() runClaimWith(theClaim = ClaimScenarioFactory.s7EmploymentMinimum(true), upToPageWithUrl = GBeenEmployedPage.url, upToIteration = 1)
      employmentData submitPage()
      val clicked = employmentData.clickLinkOrButton("#beenEmployed_yes")
      clicked.source must not contain "warningMessageWrap"
    }

    "show warning when got maximum jobs and click yes to add another" in new WithJsBrowser with PageObjects {
      val employmentData = GClaimDatePage(context) goToThePage() runClaimWith(theClaim = ClaimScenarioFactory.s7EmploymentMaximum(true), upToPageWithUrl = GBeenEmployedPage.url, upToIteration = 5)
      employmentData goToThePage()
      val clicked = employmentData.clickLinkOrButton("#beenEmployed_yes")
      clicked.source must contain("maxEmpWarningWrap")
      clicked visible ("#maxEmpWarningWrap") must beTrue
    }

    "hide warning when got maximum breaks and click yes then no" in new WithJsBrowser with PageObjects {
      val employmentData = GClaimDatePage(context) goToThePage() runClaimWith(theClaim = ClaimScenarioFactory.s7EmploymentMaximum(true), upToPageWithUrl = GBeenEmployedPage.url, upToIteration = 5)
      employmentData submitPage()
      val clickedYes = employmentData.clickLinkOrButton("#beenEmployed_yes")
      clickedYes visible ("#maxEmpWarningWrap") must beTrue
      val clickedNo = clickedYes.clickLinkOrButton("#beenEmployed_no")
      clickedNo.source must contain("maxEmpWarningWrap")
      clickedNo visible ("#maxEmpWarningWrap") must beFalse
    }
  }
  section("integration", models.domain.Employed.id)
}

trait EmployedHistoryPage extends GClaimDatePageContext {
  this: WithBrowser[_] =>

  def goToHistoryPage(employmentData:TestData) = {
    val claim = ClaimScenarioFactory.s12ClaimDate()
    page goToThePage()
    page fillPageWith claim
    page submitPage()

    employmentData.EmploymentHaveYouBeenEmployedAtAnyTime_0 = "Yes"
    employmentData.EmploymentHaveYouBeenSelfEmployedAtAnyTime = "No"
    employmentData.YourIncomeNone = "true"

    val employmentPage = page goToPage new GYourIncomePage(PageObjectsContext(browser))
    employmentPage fillPageWith employmentData
    val jobDetailsPage = employmentPage submitPage()
    jobDetailsPage fillPageWith employmentData
    val lastWage = jobDetailsPage submitPage()
    lastWage fillPageWith employmentData
    val pensionSchmesPage = lastWage submitPage()
    pensionSchmesPage fillPageWith employmentData
    val expensesPage = pensionSchmesPage submitPage()
    expensesPage fillPageWith employmentData
    val historyPage = expensesPage submitPage()
    historyPage
  }
}
