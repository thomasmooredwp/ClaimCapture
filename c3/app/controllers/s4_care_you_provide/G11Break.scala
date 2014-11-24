package controllers.s4_care_you_provide

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import controllers.Mappings._
import utils.helpers.CarersForm._
import models.domain.BreaksInCare
import models.view.CachedClaim
import play.api.data.FormError
import models.domain.Break
import G10BreaksInCare.breaksInCare
import controllers.CarersForms._


object G11Break extends Controller with CachedClaim {
  val form = Form(mapping(
    "breakID" -> carersNonEmptyText,
    "start" -> (dayMonthYear verifying validDateTime),
    "end" -> optional(dayMonthYear verifying validDateTimeOnly),
    "whereYou" -> whereabouts.verifying(requiredWhereabouts),
    "wherePerson" -> whereabouts.verifying(requiredWhereabouts),
    "medicalDuringBreak" -> carersNonEmptyText
  )(Break.apply)(Break.unapply))

  val backCall = routes.G10BreaksInCare.present()

  def present = claimingWithCheck {implicit claim =>  implicit request =>  lang =>
    Ok(views.html.s4_care_you_provide.g11_break(form,backCall)(lang))
  }

  def submit = claimingWithCheck {implicit claim =>  implicit request =>  lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val fwe = formWithErrors
        .replaceError("whereYou.location", "error.required", FormError("whereYou","error.required",Seq("This is field required")))
        .replaceError("wherePerson.location", "error.required", FormError("wherePerson","error.required",Seq("This field is required")))
        .replaceError("start.date","error.required", FormError("start","error.required", Seq("This field is required")))
        .replaceError("whereYou.location.other","error.maxLength",FormError("whereYou","error.maxLength"))
        .replaceError("wherePerson.location.other","error.maxLength",FormError("wherePerson","error.maxLength"))
        BadRequest(views.html.s4_care_you_provide.g11_break(fwe,backCall)(lang))
      },
      break => {
        val updatedBreaksInCare = if (breaksInCare.breaks.size >= 10) breaksInCare else breaksInCare.update(break)
        claim.update(updatedBreaksInCare) -> Redirect(routes.G10BreaksInCare.present())
      })
  }


 def break(id: String) = claimingWithCheck {implicit claim =>  implicit request =>  lang =>
    claim.questionGroup(BreaksInCare) match {
      case Some(b: BreaksInCare) => b.breaks.find(_.id == id) match {
        case Some(b: Break) => Ok(views.html.s4_care_you_provide.g11_break(form.fill(b),backCall)(lang))
        case _ => Redirect(routes.G10BreaksInCare.present())
      }

      case _ => Redirect(routes.G10BreaksInCare.present())
    }
  }
}