package controllers.s_self_employment

import org.specs2.mutable._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import models.domain._
import models.view.CachedClaim
import utils.WithApplication

class GPensionAndExpensesSpec extends Specification {
  section("unit", models.domain.SelfEmployment.id)
  "Pension and expenses" should {
    "present" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedClaim.key -> claimKey)
      val result = GSelfEmploymentPensionsAndExpenses.present(request)
      status(result) mustEqual OK
    }

    "require all mandatory data" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedClaim.key -> claimKey)

      val result = GSelfEmploymentPensionsAndExpenses.submit(request)
      status(result) mustEqual BAD_REQUEST
    }

    "accept all mandatory data" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedClaim.key -> claimKey).withFormUrlEncodedBody(
        "payPensionScheme.answer" -> "yes",
        "haveExpensesForJob.answer" -> "yes",
        "payPensionScheme.text" -> "some pension expense",
        "haveExpensesForJob.text" -> "some job expense"
      )
      val result = GSelfEmploymentPensionsAndExpenses.submit(request)
      status(result) mustEqual SEE_OTHER
    }
  }
  section("unit", models.domain.SelfEmployment.id)
}
