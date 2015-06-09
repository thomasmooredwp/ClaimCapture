package utils.pageobjects.circumstances.s1_start_of_process

import utils.pageobjects.{PageContext, CircumstancesPage, PageObjectsContext}
import utils.WithBrowser

final class G1ReportChangesPage(ctx:PageObjectsContext) extends CircumstancesPage(ctx, G1ReportChangesPage.url) {
  declareRadioList("#reportChanges", "CircumstancesReportChanges")
}

/**
 * Companion object that integrates factory method.
 * It is used by PageFactory object defined in PageFactory.scala
 */
object G1ReportChangesPage {
  val url  = "/circumstances/report-changes/selection"

  def apply(ctx:PageObjectsContext) = new G1ReportChangesPage(ctx)
}

/** The context for Specs tests */
trait G1ReportChangePagesContext extends PageContext {
  this: WithBrowser[_] =>

  val page = G1ReportChangesPage(PageObjectsContext(browser))
}