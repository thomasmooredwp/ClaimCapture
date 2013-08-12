package utils.pageobjects.s4_care_you_provide

import play.api.test.{WithBrowser, TestBrowser}
import utils.pageobjects.{PageContext, Page}

/**
 * Page object for s4_care_you_provide g2_their_contact_details.
 * @author Saqib Kayani
 *         Date: 25/07/2013
 */
class G2TheirContactDetailsPage (browser: TestBrowser, previousPage: Option[Page] = None) extends Page(browser, G2TheirContactDetailsPage.url, G2TheirContactDetailsPage.title, previousPage) {
  declareAddress("#address", "AboutTheCareYouProvideAddressPersonCareFor")
  declareInput("#postcode", "AboutTheCareYouProvidePostcodePersonCareFor")
  declareInput("#phoneNumber", "AboutTheCareYouProvideDaytimePhoneNumberPersonYouCare")
}

/**
 * Companion object that integrates factory method.
 * It is used by PageFactory object defined in PageFactory.scala
 */
object G2TheirContactDetailsPage {
  val title = "Contact details of the Person you care for - About the care you provide"

  val url  = "/care-you-provide/their-contact-details"

  def buildPageWith(browser: TestBrowser, previousPage: Option[Page] = None) = new G2TheirContactDetailsPage(browser,previousPage)
}

/** The context for Specs tests */
trait G2TheirContactDetailsPageContext extends PageContext {
  this: WithBrowser[_] =>

  val page = G2TheirContactDetailsPage buildPageWith browser
}