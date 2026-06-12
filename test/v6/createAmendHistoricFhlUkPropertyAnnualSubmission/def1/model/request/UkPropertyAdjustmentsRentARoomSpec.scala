/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v6.createAmendHistoricFhlUkPropertyAnnualSubmission.def1.model.request

import api.utils.UnitSpec
import play.api.libs.json.{JsValue, Json}

class UkPropertyAdjustmentsRentARoomSpec extends UnitSpec {

  val requestBody: UkPropertyAdjustmentsRentARoom =
    UkPropertyAdjustmentsRentARoom(true)

  val validJson: JsValue = Json.parse("""
      |{
      |    "jointlyLet": true
      |}
      |""".stripMargin)

  val requestBodyFalse: UkPropertyAdjustmentsRentARoom =
    UkPropertyAdjustmentsRentARoom(false)

  val validJsonFalse: JsValue = Json.parse("""
      |{
      |    "jointlyLet": false
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validJson.as[UkPropertyAdjustmentsRentARoom] shouldBe requestBody
        validJsonFalse.as[UkPropertyAdjustmentsRentARoom] shouldBe requestBodyFalse
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe validJson
        Json.toJson(requestBodyFalse) shouldBe validJsonFalse
      }
    }
  }

  "UkPropertyAdjustmentsRentARoom" when {
    "testing case class operations" should {
      "support equality comparison" in {
        val copy1     = UkPropertyAdjustmentsRentARoom(true)
        val copy2     = UkPropertyAdjustmentsRentARoom(true)
        val different = UkPropertyAdjustmentsRentARoom(false)

        copy1 shouldBe copy2
        copy1 should not be different
      }

      "support copy method" in {
        val original = UkPropertyAdjustmentsRentARoom(true)
        val copied   = original.copy(jointlyLet = false)

        copied.jointlyLet shouldBe false
        original.jointlyLet shouldBe true
      }

      "test toString representation" in {
        val model = UkPropertyAdjustmentsRentARoom(true)
        model.toString should include("true")
      }

      "support hashCode" in {
        val copy1     = UkPropertyAdjustmentsRentARoom(true)
        val copy2     = UkPropertyAdjustmentsRentARoom(true)
        val different = UkPropertyAdjustmentsRentARoom(false)

        copy1.hashCode() shouldBe copy2.hashCode()
        copy1.hashCode() should not equal different.hashCode()
      }
    }
  }

}
