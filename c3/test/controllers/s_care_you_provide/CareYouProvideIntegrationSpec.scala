package controllers.s_care_you_provide

import org.specs2.mutable._
import utils.WithBrowser
import controllers.ClaimScenarioFactory
import utils.pageobjects.PageObjects
import utils.pageobjects.breaks_in_care.GBreaksInCareSummaryPage
import utils.pageobjects.s_care_you_provide.GTheirPersonalDetailsPage
import utils.pageobjects.s_education.GYourCourseDetailsPage

class CareYouProvideIntegrationSpec extends Specification {
  section("integration", models.domain.CareYouProvide.id)
  "Care you provide" should {
    """navigate to page personal details""" in new WithBrowser with PageObjects {
      val page = GTheirPersonalDetailsPage(context)
      page goToThePage()
    }

    "navigate to breaks summary page if all details provided" in new WithBrowser with PageObjects {
      val claim = ClaimScenarioFactory.s4CareYouProvide(true,true)
      val page = GTheirPersonalDetailsPage(context)
      page goToThePage()
      val breakPage = page runClaimWith(claim,GBreaksInCareSummaryPage.url)
      breakPage must beAnInstanceOf[GBreaksInCareSummaryPage]
    }
  }
  section("integration", models.domain.CareYouProvide.id)
}
