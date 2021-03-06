package controllers.s_pay_details

import org.specs2.mutable._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import models.domain.Claiming
import models.view.CachedClaim
import utils.WithApplication
import utils.pageobjects.s_information.GAdditionalInfoPage

class GHowWePayYouSpec extends Specification {
  section("unit", models.domain.PayDetails.id)
  "How we pay you" should {
      "present" in new WithApplication with Claiming {
        val request = FakeRequest().withSession(CachedClaim.key -> claimKey)

        val result = GHowWePayYou.present(request)
        status(result) mustEqual OK
      }

      """enforce answer to "How would you like to be paid?" and "How often do you want to get paid?".""" in new WithApplication with Claiming {
        val request = FakeRequest().withSession(CachedClaim.key -> claimKey)

        val result = GHowWePayYou.submit(request)
        status(result) mustEqual BAD_REQUEST
      }

      """accept customer gets paid by bank account or building society""" in new WithApplication with Claiming {
        val request = FakeRequest().withSession(CachedClaim.key -> claimKey)
          .withFormUrlEncodedBody("likeToPay" -> "01", // todo - 01 does not mean anything...
            "paymentFrequency" -> "Every four weeks")

        val result = GHowWePayYou.submit(request)

        redirectLocation(result) must beSome(GAdditionalInfoPage.url)
      }

      "pass after filling all fields" in new WithApplication with Claiming {
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "likeToPay" -> "yes",
            "paymentFrequency" -> "1W",
            "bankDetails.accountHolderName" -> "some Holder",
            "bankDetails.bankFullName" -> "some bank",
            "bankDetails.sortCode.sort1" -> "10",
            "bankDetails.sortCode.sort2" -> "10",
            "bankDetails.sortCode.sort3" -> "10",
            "bankDetails.accountNumber" -> "12345678",
            "bankDetails.rollOrReferenceNumber" -> "1234567899",
            "bankDetails.whoseNameIsTheAccountIn" -> "Your name"
          )

        val result2 = GHowWePayYou.submit(request)
        status(result2) mustEqual SEE_OTHER
      }

      "allow spaces in account number" in new WithApplication with Claiming {
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "likeToPay" -> "yes",
            "paymentFrequency" -> "1W",
            "bankDetails.accountHolderName" -> "some Holder",
            "bankDetails.bankFullName" -> "some bank",
            "bankDetails.sortCode.sort1" -> "10",
            "bankDetails.sortCode.sort2" -> "10",
            "bankDetails.sortCode.sort3" -> "10",
            "bankDetails.accountNumber" -> " 1234 5678",
            "bankDetails.rollOrReferenceNumber" -> "1234567899",
            "bankDetails.whoseNameIsTheAccountIn" -> "Your name"
          )

        val result = GHowWePayYou.submit(request)
        status(result) mustEqual SEE_OTHER
      }

      "block letters in account number" in new WithApplication with Claiming {
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "likeToPay" -> "yes",
            "paymentFrequency" -> "1W",
            "bankDetails.accountHolderName" -> "some Holder",
            "bankDetails.bankFullName" -> "some bank",
            "bankDetails.sortCode.sort1" -> "10",
            "bankDetails.sortCode.sort2" -> "10",
            "bankDetails.sortCode.sort3" -> "10",
            "bankDetails.accountNumber" -> "1234567X",
            "bankDetails.rollOrReferenceNumber" -> "1234567899",
            "bankDetails.whoseNameIsTheAccountIn" -> "Your name"
          )

        val result = GHowWePayYou.submit(request)
        status(result) mustEqual BAD_REQUEST
      }

    "correct error shown when account is empty" in new WithApplication with Claiming {
      val request = FakeRequest()
        .withFormUrlEncodedBody(
          "likeToPay" -> "yes",
          "paymentFrequency" -> "1W",
          "bankDetails.accountHolderName" -> "some Holder",
          "bankDetails.bankFullName" -> "some bank",
          "bankDetails.sortCode.sort1" -> "10",
          "bankDetails.sortCode.sort2" -> "10",
          "bankDetails.sortCode.sort3" -> "10",
          "bankDetails.rollOrReferenceNumber" -> "1234567899",
          "bankDetails.whoseNameIsTheAccountIn" -> "Your name",
          "bankDetails.accountNumber" -> ""
        )

      val result = GHowWePayYou.submit(request)
      val source = contentAsString(result)
      status(result) mustEqual BAD_REQUEST
      source must contain("Account number - Enter your bank account number")
    }

    "correct error shown when account has none numerics" in new WithApplication with Claiming {
      val request = FakeRequest()
        .withFormUrlEncodedBody(
          "likeToPay" -> "yes",
          "paymentFrequency" -> "1W",
          "bankDetails.accountHolderName" -> "some Holder",
          "bankDetails.bankFullName" -> "some bank",
          "bankDetails.sortCode.sort1" -> "10",
          "bankDetails.sortCode.sort2" -> "10",
          "bankDetails.sortCode.sort3" -> "10",
          "bankDetails.rollOrReferenceNumber" -> "1234567899",
          "bankDetails.whoseNameIsTheAccountIn" -> "Your name",
          "bankDetails.accountNumber" -> "1234A"
        )

      val result = GHowWePayYou.submit(request)
      val source = contentAsString(result)
      status(result) mustEqual BAD_REQUEST
      source must contain("Account number - You must only enter numbers")
    }

    "correct error shown when account is less than 6 chars" in new WithApplication with Claiming {
      val request = FakeRequest()
        .withFormUrlEncodedBody(
          "likeToPay" -> "yes",
          "paymentFrequency" -> "1W",
          "bankDetails.accountHolderName" -> "some Holder",
          "bankDetails.bankFullName" -> "some bank",
          "bankDetails.sortCode.sort1" -> "10",
          "bankDetails.sortCode.sort2" -> "10",
          "bankDetails.sortCode.sort3" -> "10",
          "bankDetails.rollOrReferenceNumber" -> "1234567899",
          "bankDetails.whoseNameIsTheAccountIn" -> "Your name",
          "bankDetails.accountNumber" -> "12345"
        )

      val result = GHowWePayYou.submit(request)
      val source = contentAsString(result)
      status(result) mustEqual BAD_REQUEST
      source must contain("Account number - Minimum length is 6")
    }
  }
  section("unit", models.domain.PayDetails.id)
}
