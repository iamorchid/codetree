package sh.will.quick.test

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import org.scalatest.WordSpec

@RunWith(classOf[JUnitRunner])
class Unittest extends WordSpec with Matchers {
  "Hello World" should {
    "be correct" in {
      "Hello World".length shouldBe 11
    }
  }
}