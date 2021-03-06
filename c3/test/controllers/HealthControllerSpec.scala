package controllers

import play.api.mvc.{Result, Controller, Results}
import play.api.test.FakeRequest
import scala.concurrent.Future
import utils.{PlaySpecification, WithApplication}

class HealthControllerSpec extends PlaySpecification with Results {

  class TestController() extends Controller with HealthController

  section("unit")
  "Health Page#health" should {
    "should be valid" in new WithApplication {
      val controller = new TestController()
      val result: Future[Result] = controller.healthReport().apply(FakeRequest())
      val bodyText: String = contentAsString(result)
      bodyText must contain("isHealthy")
    }

  }
  section("unit")

  section("unit")
  "Ping" should {
    "always return ok" in new WithApplication {
      val controller = new TestController()
      val result: Future[Result] = controller.ping().apply(FakeRequest())
      status(result) mustEqual OK
    }
  }
  section("unit")
}




