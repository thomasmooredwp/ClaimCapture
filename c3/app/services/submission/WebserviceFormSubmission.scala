package services.submission

import app.ConfigProperties._
import play.api.libs.ws.WS
import play.api.libs.ws
import scala.concurrent.Future
import play.Configuration
import scala.xml.Elem
import play.api.Logger
import services.util.CharacterStripper

class WebserviceFormSubmission extends FormSubmission {

  def submitClaim(claimSubmission: Elem): Future[ws.Response] = {
    val submissionServerEndpoint: String =
      Configuration.root().getString("submissionServerUrl", "SubmissionServerEndpointNotSet") + "submit/claim"
    Logger.debug(s"Submission Server : $submissionServerEndpoint")
    val result = WS.url(submissionServerEndpoint)
      .withHeaders(("Content-Type", "text/xml"))
      .post(CharacterStripper.stripNonPdf(claimSubmission.buildString(stripComments = true)))
    result
  }

  def retryClaim(claimRetry: Elem): Future[ws.Response] = {
    val retryServerEndpoint: String =getProperty("submissionServerUrl", "SubmissionServerEndpointNotSet") + "retry/claim"
    Logger.debug(s"Submission Server retry : $retryServerEndpoint")
    val result = WS.url(retryServerEndpoint)
      .withHeaders(("Content-Type", "text/xml"))
      .post(claimRetry.buildString(stripComments = true))
    result
  }
}
