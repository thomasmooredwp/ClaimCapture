package controllers.s2_about_you

import org.specs2.mutable.{Tags, Specification}
import play.api.test.WithBrowser
import utils.pageobjects.s2_about_you.YourDetailsPageContext
import utils.pageobjects.s1_carers_allowance.ApprovePage
import utils.pageobjects.{ClaimScenario, PageObjectException}
import controllers.ClaimScenarioFactory

class G1YourDetailsIntegrationSpec extends Specification with Tags {

  "Your Details" should {
    "be presented" in new WithBrowser with YourDetailsPageContext {
      page goToThePage()
    }

    "navigate back to approve page" in new WithBrowser with YourDetailsPageContext {
      page goToThePage()
      val backPage = page goBack()
      backPage must beAnInstanceOf[ApprovePage]
    }

    "present errors if mandatory fields are not populated" in new WithBrowser with YourDetailsPageContext {
      page goToThePage()
      page submitPage() must throwA[PageObjectException]
    }

    "Accept submit if all mandatory fields are populated" in new WithBrowser with YourDetailsPageContext {
      val claim = ClaimScenarioFactory.yourDetailsWithNotTimeOutside
      page goToThePage()
      page fillPageWith claim
      page submitPage()
    }

  } section "integration"
}