package controllers.circs.s2_report_changes

import play.api.test.WithBrowser
import utils.pageobjects.circumstances.s2_report_changes._
import controllers.CircumstancesScenarioFactory
import org.specs2.mutable.{Tags, Specification}
import utils.pageobjects.PageObjects
import utils.pageobjects.circumstances.s2_report_changes.G4OtherChangeInfoPage
import utils.pageobjects.circumstances.s1_about_you.G1ReportAChangeInYourCircumstancesPage
import utils.pageobjects.circumstances.s3_consent_and_declaration.G1DeclarationPage

class G5PaymentChangeIntegrationSpec extends Specification with Tags {

   "Report a change in your circumstances" should {

     "be presented" in new WithBrowser with PageObjects{
       val page =  G5PaymentChangePage(context)
       page goToThePage()
     }

     "navigate to previous page" in new WithBrowser with PageObjects{
       val page =  G1ReportChangesPage(context)
       page goToThePage()

       val claim = CircumstancesScenarioFactory.paymentChangesChangeInfo
       page fillPageWith(claim)
       val completedPage = page submitPage()
       println("*** 1: " + completedPage.pageTitle)

       val reportChangesPage = completedPage runClaimWith (claim, G5PaymentChangePage.title)
       println("*** 2: " + reportChangesPage.pageTitle)

       reportChangesPage must beAnInstanceOf[G5PaymentChangePage]

       val prevPage = reportChangesPage.goBack()
       println("*** 3: " + prevPage.pageTitle)
       prevPage must beAnInstanceOf[G1ReportChangesPage]
     }

     "navigate to next page when 'yes' is selected for currently paid into bank selected" in new WithBrowser with PageObjects{
       val page =  G5PaymentChangePage(context)
       val claim = CircumstancesScenarioFactory.reportChangesPaymentChangeScenario1
       page goToThePage()
       page fillPageWith claim

       val nextPage = page submitPage ()
       nextPage must beAnInstanceOf[G1DeclarationPage]
     }

     "navigate to next page when 'no' is selected for currently paid into bank selected" in new WithBrowser with PageObjects{
       val page =  G5PaymentChangePage(context)
       val claim = CircumstancesScenarioFactory.reportChangesPaymentChangeScenario2
       page goToThePage()
       page fillPageWith claim

       val nextPage = page submitPage ()
       nextPage must beAnInstanceOf[G1DeclarationPage]
     }

   } section("integration", models.domain.CircumstancesIdentification.id)

 }
