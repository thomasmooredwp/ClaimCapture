package models.domain

import org.specs2.mutable._
import org.specs2.mock.Mockito
import utils.WithApplication

class BreaksInCareSpec extends Specification with Mockito {
  section("unit")
  "Breaks from care" should {
    "give zero breaks upon deleting from no existing breaks in care" in new WithApplication {
      val breaksInCare = OldBreaksInCare()

      val updatedBreaksInCare = breaksInCare delete "non existing break ID"
      updatedBreaksInCare.breaks.size mustEqual 0
    }

    "give zero breaks upon deleting the only break" in new WithApplication {
      val break = mock[OldBreak]
      break.iterationID returns "breakID"

      val breaksInCare = OldBreaksInCare().update(break)
      breaksInCare.breaks.size mustEqual 1

      val updatedBreaksInCare = breaksInCare delete break.iterationID
      updatedBreaksInCare.breaks.size mustEqual 0
    }

    "give 2 breaks upon deleting 2nd out of 3 breaks" in new WithApplication {
      val break1 = mock[OldBreak]
      break1.iterationID returns "break1ID"

      val break2 = mock[OldBreak]
      break2.iterationID returns "break2ID"

      val break3 = mock[OldBreak]
      break3.iterationID returns "break3ID"

      val breaksInCare = OldBreaksInCare().update(break1).update(break2).update(break3)
      breaksInCare.breaks.size mustEqual 3

      val updatedBreaksInCare = breaksInCare delete break2.iterationID
      updatedBreaksInCare.breaks.size mustEqual 2
    }
  }
  section("unit")
}
