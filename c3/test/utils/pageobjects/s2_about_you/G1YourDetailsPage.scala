package utils.pageobjects.s2_about_you

import utils.WithBrowser
import utils.pageobjects._

/**
 * PageObject for page s2_about_you g1_yourDetails.
 * @author Jorge Migueis
 *         Date: 09/07/2013
 */
final class G1YourDetailsPage(ctx:PageObjectsContext) extends ClaimPage(ctx, G1YourDetailsPage.url) {
  declareRadioList("#title", "AboutYouTitle")
  declareInput("#titleOther", "AboutYouTitleOther")
  declareInput("#firstName","AboutYouFirstName")
  declareInput("#middleName","AboutYouMiddleName")
  declareInput("#surname","AboutYouSurname")
  declareNino("#nationalInsuranceNumber","AboutYouNINO")
  declareDate("#dateOfBirth", "AboutYouDateOfBirth")
}

/**
 * Companion object that integrates factory method.
 * It is used by PageFactory object defined in PageFactory.scala
 */
object G1YourDetailsPage {
  val url  = "/about-you/your-details"

  def apply(ctx:PageObjectsContext) = new G1YourDetailsPage(ctx)
}

/** The context for Specs tests */
trait G1YourDetailsPageContext extends PageContext {
  this: WithBrowser[_] =>

  val page = G1YourDetailsPage (PageObjectsContext(browser))
}