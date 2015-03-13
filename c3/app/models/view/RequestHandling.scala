package models.view

import app.ConfigProperties._
import play.api.Logger
import play.api.http.HeaderNames._
import play.api.mvc.Results._
import play.api.mvc.{Result, AnyContent, Request, Call}

trait RequestHandling {

  def startPage: String
  def timeoutPage: Call
  def errorPageCookie: Call
  def errorPage: Call
  def errorPageBrowserBackButton: Call

  // CSRF Cookie management
  protected val csrfCookieName = getProperty("csrf.cookie.name", "csrf")
  protected val csrfSecure = getProperty("csrf.cookie.secure", getProperty("session.secure", default = false))

  protected val defaultLang = "en"
  protected lazy val redirectEnforced = getProperty("enforceRedirect", default = true)

  protected def originCheck(action: => Result)(implicit request: Request[AnyContent]) = {
    val (referer, host) = request.headers.get("Referer").getOrElse("No Referer in header") -> request.headers.get("Host").getOrElse("No Host in header")
    val sameHostCheck = referer.contains(host)

    Logger.info(s"Redirect $redirectEnforced. Same host $sameHostCheck")
    if (sameHostCheck) {
      withHeaders(action)
    } else {
      if (redirectEnforced) {
        Logger.warn(s"HTTP Referrer : $referer. Conf Referrer : $startPage. HTTP Host : $host")
        MovedPermanently(startPage)
      } else {
        withHeaders(action)
      }
    }
  }

  protected def withHeaders(result: Result): Result = {
    result.withHeaders(CACHE_CONTROL -> "must-revalidate,no-cache,no-store", "X-Frame-Options" -> "SAMEORIGIN")
  }

}