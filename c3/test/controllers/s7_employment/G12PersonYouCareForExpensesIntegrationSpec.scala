package controllers.s7_employment

import org.specs2.mutable.{Tags, Specification}
import play.api.test.WithBrowser
import controllers.ClaimScenarioFactory
import utils.pageobjects.s7_employment._
import utils.pageobjects.PageObjects

class G12PersonYouCareForExpensesIntegrationSpec extends Specification with Tags {
  "Expenses related to the person you care for, while you are at work - Integration" should {
    "be presented" in new WithBrowser with PageObjects{
			val page =  G8AboutExpensesPage(context)
      val claim = ClaimScenarioFactory s7Employment()
      claim.EmploymentDoYouPayforAnythingNecessaryToDoYourJob_1="no"
      claim.EmploymentDoYouPayAnyoneLookAfterYourChild_1="no"
      page goToThePage()
      page fillPageWith claim
      page submitPage()
    }

    "contain 1 completed form" in new WithBrowser with PageObjects{
			val page =  G8AboutExpensesPage(context)
      val claim = ClaimScenarioFactory s7Employment()
      claim.EmploymentDoYouPayforAnythingNecessaryToDoYourJob_1="no"
      claim.EmploymentDoYouPayAnyoneLookAfterYourChild_1="no"
      page goToThePage()
      page fillPageWith claim
      val p = page submitPage()
      p fillPageWith claim
      p submitPage() match {
        case p: G14JobCompletionPage => p numberSectionsCompleted()  mustEqual 2
        case _ => ko("Next Page is not of the right type.")
      }
    }

    "be able to navigate back to a completed form" in new WithBrowser  with PageObjects{
			val page =  G8AboutExpensesPage(context)
      val claim = ClaimScenarioFactory s7Employment()
      claim.EmploymentDoYouPayforAnythingNecessaryToDoYourJob_1="no"
      claim.EmploymentDoYouPayAnyoneLookAfterYourChild_1="no"
      page goToThePage()
      page fillPageWith claim
      val p = page submitPage()
      p match {
        case p: G12PersonYouCareForExpensesPage => p numberSectionsCompleted()  mustEqual 1
        case _ => ko("Next Page is not of the right type.")
      }
      p goBack() must beAnInstanceOf[G8AboutExpensesPage]
    }
  } section("integration",models.domain.Employed.id)
}