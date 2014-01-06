package controllers.submission

import scala.concurrent.Future
import play.api.mvc.{SimpleResult, AnyContent, Request}
import models.domain.Claim

trait Submitter {
  def submit(claim: Claim, request : Request[AnyContent]): Future[SimpleResult]
}