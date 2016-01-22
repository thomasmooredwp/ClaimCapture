package controllers.circs.start_of_process

import utils.WithApplication
import org.specs2.mutable._
import utils.{LightFakeApplication, WithBrowser}
import controllers.CircumstancesScenarioFactory
import utils.pageobjects.circumstances.start_of_process.{GReportChangesPage, GReportAChangeInYourCircumstancesPage}
import utils.pageobjects.circumstances.report_changes.{GBreaksInCarePage, GPermanentlyStoppedCaringPage, GSelfEmploymentPage, GOtherChangeInfoPage}
import utils.pageobjects.{PageObjects, TestData}

class GReportAChangeInYourCircumstancesIntegrationSpec extends Specification {

  section("integration", models.domain.CircumstancesReportChanges.id)
  "About You" should {
    val fullName = "Mr John Joe Smith"
    val nino = "ab123456c"
    val dateOfBirth = "05/12/1990"
    val theirFullName = "Mrs Jane Smith"
    val theirRelationshipToYou = "Wife"

    val byPost = "01254 123456"
    val wantsEmailContact = "No"


    "be presented" in new WithBrowser with PageObjects {
      val page = GReportAChangeInYourCircumstancesPage(context)
      page goToThePage()
    }

    "navigate to previous page" in new WithBrowser with PageObjects{
      val page =  GReportChangesPage(context)
      val claim = CircumstancesScenarioFactory.reportChangesStoppedCaring
      page goToThePage()
      page fillPageWith claim

      val reportAChange = page submitPage()

      reportAChange must beAnInstanceOf[GReportAChangeInYourCircumstancesPage]

      reportAChange.goBack() must beAnInstanceOf[GReportChangesPage]
    }

    "present errors if mandatory fields are not populated" in new WithBrowser with PageObjects {
      val page = GReportAChangeInYourCircumstancesPage(context)
      page goToThePage()
      page.submitPage().listErrors.size mustEqual 7
    }

    "Accept submit if all mandatory fields are populated" in new WithBrowser with PageObjects {
      val page = GReportAChangeInYourCircumstancesPage(context)
      val claim = CircumstancesScenarioFactory.aboutDetails
      page goToThePage()
      page fillPageWith claim

      page submitPage()
    }

    "contain errors on invalid submission" in new WithApplication {
      "missing fullName field" in new WithBrowser with PageObjects {
        val page = GReportAChangeInYourCircumstancesPage(context)
        val claim = new TestData
        claim.CircumstancesAboutYouNationalInsuranceNumber = nino
        claim.CircumstancesAboutYouDateOfBirth = dateOfBirth
        claim.CircumstancesAboutYouTheirFullName = theirFullName
        claim.CircumstancesAboutYouTheirRelationshipToYou = theirRelationshipToYou
        claim.FurtherInfoContact = "01254 785678"
        claim.CircumstancesDeclarationWantsEmailContact = "no"


        page goToThePage()
        page fillPageWith claim

        val errors = page.submitPage().listErrors
        errors.size mustEqual 1
        errors(0) must contain("Full name - You must complete this section")
      }

      "missing nino field" in new WithBrowser with PageObjects {
        val page = GReportAChangeInYourCircumstancesPage(context)
        val claim = new TestData
        claim.CircumstancesAboutYouFullName = fullName
        claim.CircumstancesAboutYouDateOfBirth = dateOfBirth
        claim.CircumstancesAboutYouTheirFullName = theirFullName
        claim.CircumstancesAboutYouTheirRelationshipToYou = theirRelationshipToYou
        claim.FurtherInfoContact = "01254 785678"
        claim.CircumstancesDeclarationWantsEmailContact = "no"

        page goToThePage()
        page fillPageWith claim

        val errors = page.submitPage().listErrors
        errors.size mustEqual 2
        errors(0) must contain("National Insurance number - You must complete this section")
      }

      "invalid nino containing numbers" in new WithBrowser with PageObjects {
        val page = GReportAChangeInYourCircumstancesPage(context)
        val claim = new TestData
        claim.CircumstancesAboutYouFullName = fullName
        claim.CircumstancesAboutYouNationalInsuranceNumber = "11abcdef1"
        claim.CircumstancesAboutYouDateOfBirth = dateOfBirth
        claim.CircumstancesAboutYouTheirFullName = theirFullName
        claim.CircumstancesAboutYouTheirRelationshipToYou = theirRelationshipToYou
        claim.FurtherInfoContact = "01254 785678"
        claim.CircumstancesDeclarationWantsEmailContact = "no"

        page goToThePage()
        page fillPageWith claim

        val errors = page.submitPage().listErrors
        errors.size mustEqual 1
        errors(0) must contain("National Insurance number - A National insurance number must be in the format VO123456D")
      }

      "missing dateOfBirth field" in new WithBrowser with PageObjects {
        val page = GReportAChangeInYourCircumstancesPage(context)
        val claim = new TestData
        claim.CircumstancesAboutYouFullName = fullName
        claim.CircumstancesAboutYouNationalInsuranceNumber = nino
        claim.CircumstancesAboutYouTheirFullName = theirFullName
        claim.CircumstancesAboutYouTheirRelationshipToYou = theirRelationshipToYou
        claim.FurtherInfoContact = "01254 785678"
        claim.CircumstancesDeclarationWantsEmailContact = "no"

        page goToThePage()
        page fillPageWith claim

        val errors = page.submitPage().listErrors
        errors.size mustEqual 1
        errors(0) must contain("Date of birth - You must complete this section")
      }

      "missing theirFullName field" in new WithBrowser with PageObjects {
        val page = GReportAChangeInYourCircumstancesPage(context)
        val claim = new TestData
        claim.CircumstancesAboutYouFullName = fullName
        claim.CircumstancesAboutYouNationalInsuranceNumber = nino
        claim.CircumstancesAboutYouDateOfBirth = dateOfBirth
        claim.CircumstancesAboutYouTheirRelationshipToYou = theirRelationshipToYou
        claim.FurtherInfoContact = "01254 785678"
        claim.CircumstancesDeclarationWantsEmailContact = "no"

        page goToThePage()
        page fillPageWith claim

        val errors = page.submitPage().listErrors
        errors.size mustEqual 1
        errors(0) must contain("Their full name - You must complete this section")
      }

      "missing fullName field" in new WithBrowser with PageObjects {
        val page = GReportAChangeInYourCircumstancesPage(context)
        val claim = new TestData
        claim.CircumstancesAboutYouFullName = fullName
        claim.CircumstancesAboutYouNationalInsuranceNumber = nino
        claim.CircumstancesAboutYouDateOfBirth = dateOfBirth
        claim.CircumstancesAboutYouTheirFullName = theirFullName
        claim.FurtherInfoContact = "01254 785678"
        claim.CircumstancesDeclarationWantsEmailContact = "no"

        page goToThePage()
        page fillPageWith claim

        val errors = page.submitPage().listErrors
        errors.size mustEqual 1
        errors(0) must contain("Their relationship to you - You must complete this section")
      }

      "navigate to next page when addition info selected" in new WithBrowser with PageObjects{
        val page =  GReportChangesPage(context)
        val claim = CircumstancesScenarioFactory.reportChangesOtherChangeInfo
        page goToThePage()
        page fillPageWith claim

        val nextPage = page submitPage ()
        nextPage must beAnInstanceOf[GReportAChangeInYourCircumstancesPage]
        nextPage fillPageWith claim
        val lastPage = nextPage submitPage ()
        lastPage must beAnInstanceOf[GOtherChangeInfoPage]
      }

      "navigate to next page when self-employment selected" in new WithBrowser(app = LightFakeApplication(additionalConfiguration = Map("circs.employment.active" -> "false"))) with PageObjects{
        val page =  GReportChangesPage(context)
        val claim = CircumstancesScenarioFactory.reportChangesSelfEmployment
        page goToThePage()
        page fillPageWith claim

        val nextPage = page submitPage ()
        nextPage must beAnInstanceOf[GReportAChangeInYourCircumstancesPage]
        nextPage fillPageWith claim
        val lastPage = nextPage submitPage ()
        lastPage must beAnInstanceOf[GSelfEmploymentPage]
      }

      "navigate to next page when stopped caring selected" in new WithBrowser with PageObjects{
        val page =  GReportChangesPage(context)
        val claim = CircumstancesScenarioFactory.reportChangesStoppedCaring
        page goToThePage()
        page fillPageWith claim

        val nextPage = page submitPage ()
        nextPage must beAnInstanceOf[GReportAChangeInYourCircumstancesPage]
        nextPage fillPageWith claim
        val lastPage = nextPage submitPage ()
        lastPage must beAnInstanceOf[GPermanentlyStoppedCaringPage]
      }

      "navigate to next page when break from caring selected" in new WithBrowser with PageObjects{
        val page =  GReportChangesPage(context)
        val claim = CircumstancesScenarioFactory.reportBreakFromCaring
        page goToThePage()
        page fillPageWith claim

        val nextPage = page submitPage ()
        nextPage must beAnInstanceOf[GReportAChangeInYourCircumstancesPage]
        nextPage fillPageWith claim
        val lastPage = nextPage submitPage ()
        lastPage must beAnInstanceOf[GBreaksInCarePage]
      }

      "valid submission if 'Contact number' not filled in" in new WithBrowser with PageObjects{
        val page =  GReportAChangeInYourCircumstancesPage(context)
        val claim = CircumstancesScenarioFactory.reportChangesOtherChangeInfo
        claim.FurtherInfoContact = ""
        page goToThePage()
        page fillPageWith claim

        val nextPage = page submitPage()
        nextPage must beAnInstanceOf[GOtherChangeInfoPage]
      }

      "valid submission if 'Contact number' is filled in with number" in new WithBrowser with PageObjects{
        val page =  GReportAChangeInYourCircumstancesPage(context)
        val claim = CircumstancesScenarioFactory.reportChangesOtherChangeInfo
        page goToThePage()
        page fillPageWith claim

        val nextPage = page submitPage()
        nextPage must beAnInstanceOf[GOtherChangeInfoPage]
      }

      "contain error if 'Contact number' is filled in with text" in new WithBrowser with PageObjects{
        val page =  GReportAChangeInYourCircumstancesPage(context)
        val claim = CircumstancesScenarioFactory.reportBreakFromCaring
        claim.FurtherInfoContact = "hjhjsddh"
        page goToThePage()
        page fillPageWith claim

        val errors = page.submitPage().listErrors
        errors.size mustEqual 1
        errors(0) must contain("Contact number - Invalid value")
      }

      "contain error if 'Contact number' is field length less than min length" in new WithBrowser with PageObjects{
        val page =  GReportAChangeInYourCircumstancesPage(context)
        val claim = CircumstancesScenarioFactory.reportBreakFromCaring
        claim.FurtherInfoContact = "012345"
        page goToThePage()
        page fillPageWith claim

        val errors = page.submitPage().listErrors
        errors.size mustEqual 1
        errors(0) must contain("Contact number - Invalid value")
      }

      "contain error if 'Contact number' is field length greater than max length" in new WithBrowser with PageObjects{
        val page =  GReportAChangeInYourCircumstancesPage(context)
        val claim = CircumstancesScenarioFactory.reportBreakFromCaring
        claim.FurtherInfoContact = "012345678901234567890"
        page goToThePage()
        page fillPageWith claim

        val errors = page.submitPage().listErrors
        errors.size mustEqual 1
        errors(0) must contain("Contact number - Invalid value")
      }
    }
  }
  section("integration", models.domain.CircumstancesReportChanges.id)
}