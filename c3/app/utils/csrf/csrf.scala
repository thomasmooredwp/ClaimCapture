package utils.csrf

import play.api.mvc._
import play.api._
import play.api.libs.Crypto
import app.ConfigProperties._

private[csrf] object CSRFConf {


  def TokenName: String = getProperty("csrf.token.name","csrfToken")
  def CookieName: Option[String] = Play.current.configuration.getString("csrf.cookie.name")
  def SecureCookie: Boolean = getProperty("csrf.cookie.secure",Session.secure)
  def PostBodyBuffer: Long = getProperty("csrf.body.bufferSize",102400L)
  def SignTokens: Boolean = getProperty("csrf.sign.tokens",true)

  val UnsafeMethods = Set("POST")
  val UnsafeContentTypes = Set("application/x-www-form-urlencoded", "text/plain", "multipart/form-data")

  val HeaderName = "Csrf-Token"
  val HeaderNoCheck = "nocheck"

  def defaultCreateIfNotFound(request: RequestHeader) = {
    // If the request isn't accepting HTML, then it won't be rendering a form, so there's no point in generating a
    // CSRF token for it.
    request.method == "GET" && (request.accepts("text/html") || request.accepts("application/xml+xhtml"))
  }
  def defaultTokenProvider = {
    if (SignTokens) {
      DwpCSRF.SignedTokenProvider
    } else {
      DwpCSRF.UnsignedTokenProvider
    }
  }
}

/**
* Original code comes from Play GitHub, filters plugin.
*/
object DwpCSRF {

  private[csrf] val filterLogger = play.api.Logger("play.filters")

  /**
   * A CSRF token
   */
  case class Token(value: String)

  object Token {
    val RequestTag = "CSRF_TOKEN"

    implicit def getToken(implicit request: RequestHeader): Token = {
      DwpCSRF.getToken(request).getOrElse(sys.error("Missing CSRF Token"))
    }
  }

  // Allows the template helper to access it
  def TokenName = CSRFConf.TokenName

  import CSRFConf._

  /**
   * Extract token from current request
   */
  def getToken(request: RequestHeader): Option[Token] = {
    // First check the tags, this is where tokens are added if it's added to the current request
    val token = request.tags.get(Token.RequestTag)
      // Check cookie if cookie name is defined
      .orElse(CookieName.flatMap(n => request.cookies.get(n).map(_.value)))
      // Check session
      .orElse(request.session.get(TokenName))
    if (SignTokens) {
      // Extract the signed token, and then resign it. This makes the token random per request, preventing the BREACH
      // vulnerability
      token.flatMap(Crypto.extractSignedToken)
        .map(token => Token(Crypto.signToken(token)))
    } else {
      token.map(Token.apply)
    }
  }

  /**
   * A token provider, for generating and comparing tokens.
   *
   * This abstraction allows the use of randomised tokens.
   */
  trait TokenProvider {
    /** Generate a token */
    def generateToken: String
    /** Compare two tokens */
    def compareTokens(tokenA: String, tokenB: String): Boolean
  }

  object SignedTokenProvider extends TokenProvider {
    def generateToken = Crypto.generateSignedToken
    def compareTokens(tokenA: String, tokenB: String) = Crypto.compareSignedTokens(tokenA, tokenB)
  }

  object UnsignedTokenProvider extends TokenProvider {
    def generateToken = Crypto.generateToken
    def compareTokens(tokenA: String, tokenB: String) = Crypto.constantTimeEquals(tokenA, tokenB)
  }
}
