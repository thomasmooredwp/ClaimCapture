package controllers.s4_care_you_provide

import play.api.mvc.Controller
import models.view.CachedClaim
import models.domain._
import controllers.Routing

object CareYouProvide extends Controller with CareYouProvideRouting with CachedClaim {
  def breaksInCare(implicit claim: Claim) = claim.questionGroup(BreaksInCare) match {
    case Some(bs: BreaksInCare) => bs
    case _ => BreaksInCare()
  }

  def completed = claiming { implicit claim => implicit request =>
    if (completedQuestionGroups.isEmpty) Redirect(routes.G1TheirPersonalDetails.present())
    else Ok(views.html.s4_care_you_provide.g12_completed(completedQuestionGroups.map(qg => qg -> route(qg))))
  }

  def submit = claiming { implicit claim => implicit request =>
    if (completedQuestionGroups.distinct.size >= 6) Redirect(claim.nextSection(models.domain.CareYouProvide).firstPage)
    else Redirect(controllers.s4_care_you_provide.routes.G1TheirPersonalDetails.present())
  }

  private def completedQuestionGroups(implicit claim: Claim): List[QuestionGroup] = {
    claim.completedQuestionGroups(models.domain.CareYouProvide)
  }
}

trait CareYouProvideRouting extends Routing {
  override def route(qgi: QuestionGroup.Identifier) = qgi match {
    case TheirPersonalDetails => routes.G1TheirPersonalDetails.present()
    case TheirContactDetails => routes.G2TheirContactDetails.present()
    case MoreAboutThePerson => routes.G3MoreAboutThePerson.present()
    case PreviousCarerPersonalDetails => routes.G4PreviousCarerPersonalDetails.present()
    case PreviousCarerContactDetails => routes.G5PreviousCarerContactDetails.present()
    case RepresentativesForPerson => routes.G6RepresentativesForThePerson.present()
    case MoreAboutTheCare => routes.G7MoreAboutTheCare.present()
    case OneWhoPaysPersonalDetails => routes.G8OneWhoPaysPersonalDetails.present()
    case ContactDetailsOfPayingPerson => routes.G9ContactDetailsOfPayingPerson.present()
    case BreaksInCare => routes.G10BreaksInCare.present()
  }
}