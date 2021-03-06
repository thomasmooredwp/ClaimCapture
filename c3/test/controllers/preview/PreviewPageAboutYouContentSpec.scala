package controllers.preview

import org.specs2.mutable._
import utils.WithBrowser
import utils.pageobjects.{TestData, PageObjectsContext, PageObjects}
import utils.pageobjects.preview.PreviewPage
import controllers.ClaimScenarioFactory
import utils.pageobjects.s_claim_date.GClaimDatePage
import utils.pageobjects.s_about_you.GPaymentsFromAbroadPage

class PreviewPageAboutYouContentSpec extends Specification {
  section("preview")
  "Preview Page" should {
    "display about you - the carer data with nationality as British" in new WithBrowser with PageObjects {
      fillAboutYouTheCarerSection(context)
      val page = PreviewPage(context)
      page goToThePage()
      val source = page.source
      source must contain("Name")
      source must contain("Mr John middlename Appleseed")
      source must contain("Date of birth")
      source must contain("03 April, 1950")
      source must contain("Address")
      source must contain("101 Clifton Street")
      source must contain("Blackpool")
      source must contain("FY1 2RW")
      source must contain("Contact number")
      source must contain("01772 888901")
      source must contain("Claim date")
      source must contain("10 October, 2016")
      source must contain("Your nationality")
      source must contain("British")
      source must contain("Have you always lived in England, Scotland or Wales?")
      source must contain("Have you been away from England, Scotland or Wales for more than 52 weeks")
      source must contain("Yes - Details provided")
      source must contain("Have you or anyone in your close family claimed or been paid any benefits or pensions from an EEA country since your claim date?")
      source must contain("Yes - Details provided")
      source must contain("Have you or anyone in your close family worked or paid national insurance in an EEA country since your claim date?")
      source must contain("Yes - Details provided")
    }

    "display about you - the carer data with nationality another country" in new WithBrowser with PageObjects {
      val claim = ClaimScenarioFactory.yourDetailsWithNotTimeOutside()
      claim.AboutYouNationalityAndResidencyNationality = "Another nationality"
      claim.AboutYouNationalityAndResidencyActualNationality = "French"
      claim.AboutYouWhatIsYourMaritalOrCivilPartnershipStatus = "Single"

      fillAboutYouTheCarerSection(context, claim)
      val page = PreviewPage(context)
      page goToThePage()
      val source = page.source
      source must not contain "British"
      source must contain("French")
      source must contain("Status")
      source must contain("Single")
    }

    "display about you - the carer data with nationality as British and other title" in new WithBrowser with PageObjects {
      val claim = ClaimScenarioFactory.yourDetailsWithNotTimeOutside()
      claim.AboutYouTitle = "Rev"
      fillAboutYouTheCarerSection(context, claim)

      val page = PreviewPage(context)
      page goToThePage()
      val source = page.source

      source must contain("Name")
      source must contain("Rev John middlename Appleseed")
      source must contain("Date of birth")
      source must contain("03 April, 1950")
      source must contain("Address")
      source must contain("101 Clifton Street")
      source must contain("Blackpool")
      source must contain("FY1 2RW")
      source must contain("Contact number")
      source must contain("01772 888901")
      source must contain("Claim date")
      source must contain("10 October, 2016")
      source must contain("Your nationality")
      source must contain("British")
      source must contain("Have you or anyone in your close family claimed or been paid any benefits or pensions from an EEA country since your claim date?")
      source must contain("Yes - Details provided")
      source must contain("Have you or anyone in your close family worked or paid national insurance in an EEA country since your claim date?")
      source must contain("Yes - Details provided")
    }

    "display all nationality page details" in new WithBrowser with PageObjects {
      val claim = yourDetailsWithAllNationalityFields()
      fillAboutYouTheCarerSection(context, claim)

      val page = PreviewPage(context)
      page goToThePage()
      browser.find("#about_you_nationality_value").getText() must contain("French")
      browser.find("#about_you_alwaysliveinuk_value").getText() must contain("No")
      browser.find("#about_you_liveinuknow").getText() must contain("Yes")
      browser.find("#about_you_arrivedinuk_value").getText() must contain("Less than 3 years ago")
      browser.find("#about_you_arrivedinukdate_value").getText() must contain("01 March")
      browser.find("#about_you_arrivedinukfrom_value").getText() must contain("Mongolia")
      browser.find("#about_you_trip52weeks_value").getText() must contain("Yes - Details provided")
    }
  }
  section("preview")

  def fillAboutYouTheCarerSection(context: PageObjectsContext, claim: TestData = ClaimScenarioFactory.yourDetailsWithNotTimeOutside()) = {
    val claimDatePage = GClaimDatePage(context)
    claimDatePage goToThePage()
    val claimDate = ClaimScenarioFactory.s12ClaimDate()
    claimDatePage fillPageWith claimDate

    val aboutYouPage = claimDatePage submitPage()
    claim.AboutYouMiddleName = "middlename"
    aboutYouPage goToThePage()
    aboutYouPage fillPageWith claim

    val maritalStatus = aboutYouPage submitPage()
    maritalStatus fillPageWith claim

    val contactDetailsPage = maritalStatus submitPage()
    contactDetailsPage fillPageWith claim

    val addressPage = contactDetailsPage submitPage()
    addressPage fillPageWith claim

    val nationalityPage = addressPage submitPage()
    nationalityPage fillPageWith claim

    val paymentFromAbroadPage = GPaymentsFromAbroadPage(context)
    paymentFromAbroadPage goToThePage()
    paymentFromAbroadPage fillPageWith ClaimScenarioFactory.otherEuropeanEconomicArea()

    paymentFromAbroadPage submitPage()
  }

  def yourDetails() = {
    val claim = ClaimScenarioFactory.s12ClaimDate()
    claim.AboutYouTitle = "Mr"
    claim.AboutYouFirstName = "John"
    claim.AboutYouSurname = "Appleseed"
    claim.AboutYouDateOfBirth = "03/04/1950"
    claim.AboutYouNINO = "AB123456C"
    claim.AboutYouAddress = "101 Clifton Street&Blackpool"
    claim.AboutYouPostcode = "FY1 2RW"
    claim.HowWeContactYou = "01772 888901"
    claim.AboutYouWantsEmailContact = "No"
    claim.AboutYouWhatIsYourMaritalOrCivilPartnershipStatus = "Single"

    claim
  }

  def yourDetailsWithAllNationalityFields() = {
    val claim = yourDetails()
    claim.AboutYouNationalityAndResidencyNationality = "Another nationality"
    claim.AboutYouNationalityAndResidencyActualNationality = "French"
    claim.AboutYouNationalityAndResidencyAlwaysLivedInUK = "No"
    claim.AboutYouNationalityAndResidencyLiveInUKNow = "Yes"
    claim.AboutYouNationalityAndResidencyArrivedInUK = "less"
    claim.AboutYouNationalityAndResidencyArrivedInUKDate = "01/03/2016"
    claim.AboutYouNationalityAndResidencyArrivedInUKFrom = "Mongolia"
    claim.AboutYouNationalityAndResidencyTrip52Weeks = "Yes"
    claim.AboutYouNationalityAndResidencyTripDetails = "2 years in Spain"

    claim
  }
}

