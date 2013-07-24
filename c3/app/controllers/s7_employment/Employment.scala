package controllers.s7_employment

import language.implicitConversions
import language.reflectiveCalls
import play.api.mvc.{AnyContent, Request, Controller}
import models.view.CachedClaim
import models.domain._
import models.domain.Claim
import play.api.data.Form
import scala.reflect.ClassTag

object Employment extends Controller with CachedClaim {
  /*implicit override def formFiller[Q <: QuestionGroup](form: Form[Q])(implicit classTag: ClassTag[Q]) = new {
    def fillInJob(qi: QuestionGroup.Identifier with Job.Identifier)(implicit claim: Claim): Form[Q] = {
      claim.questionGroup(qi) match {
        case Some(q: Q) => form.fill(q)
        case _ => form
      }
    }
  }*/

  def jobs(implicit claim: Claim) = claim.questionGroup(Jobs) match {
    case Some(js: Jobs) => js
    case _ => Jobs()
  }

  def completedQuestionGroups(questionGroupIdentifier: QuestionGroup.Identifier)(implicit claim: Claim, request: Request[AnyContent]) = {
    claim.questionGroup(Jobs) match {
      case Some(js: Jobs) => js.jobs.find(_.jobID == request.flash("jobID")) match {
        case Some(j: Job) => j.questionGroups.takeWhile(_.identifier.index < questionGroupIdentifier.index)
        case _ => Nil
      }
      case _ => Nil
    }
  }

  def completed = claiming { implicit claim => implicit request =>
    Ok("")
  }

  def submit = claiming { implicit claim => implicit request =>
    Redirect("")
  }
}