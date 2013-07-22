package controllers.s7_employment

import language.implicitConversions
import language.reflectiveCalls
import play.api.mvc.{Result, AnyContent, Request, Controller}
import models.view.CachedClaim
import models.domain._
import models.domain.Claim

object Employment extends Controller with CachedClaim {
  def jobs(implicit claim: Claim) = claim.questionGroup(Jobs) match {
    case Some(js: Jobs) => js
    case _ => Jobs()
  }

  implicit def inJob(result: Result) = new {
    def inJob(qg: QuestionGroup with Job.Identifier) = result.flashing("jobID" -> qg.jobID)
  }

  def completedQuestionGroups(questionGroupIdentifier: QuestionGroup.Identifier)(implicit claim: Claim, request: Request[AnyContent]) = {
    claim.questionGroup(Jobs) match {
      case Some(js: Jobs) => js.jobs.find(_.jobID == request.flash("jobID")) match {
        case Some(j: Job) => j.questionGroups.takeWhile(_.identifier.index < questionGroupIdentifier.index)
        case _ => Nil
      }
      case _ => Nil
    }

    claim.completedQuestionGroups(Jobs)
  }

  def completed = claiming { implicit claim => implicit request =>
    Ok("")
  }

  def submit = claiming { implicit claim => implicit request =>
    Redirect("")
  }
}