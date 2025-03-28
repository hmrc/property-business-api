/*
 * Copyright 2023 HM Revenue & Customs
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

package v6.historicNonFhlUkPropertyPeriodSummary.create.def1.model.request

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class UkPropertyExpensesRentARoomSpec extends UnitSpec {

  val requestBody: UkPropertyExpensesRentARoom =
    UkPropertyExpensesRentARoom(Some(947.66))

  val validJson: JsValue = Json.parse("""
      |{
      |    "amountClaimed": 947.66
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validJson.as[UkPropertyExpensesRentARoom] shouldBe requestBody
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe validJson
      }
    }
  }

}
