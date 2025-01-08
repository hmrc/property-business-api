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

package v4.historicFhlUkPropertyPeriodSummary.amend.def1.model.request

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1.model.request.UkPropertyIncomeRentARoom

class UkFhlPropertyIncomeSpec extends UnitSpec {
  val rentARoom: UkPropertyIncomeRentARoom = UkPropertyIncomeRentARoom(Some(412.89))
  val requestBody: UkFhlPropertyIncome     = UkFhlPropertyIncome(Some(215.16), Some(1365.12), Some(rentARoom))

  val mtdJson: JsValue = Json.parse("""
      |{
      |    "periodAmount":    215.16,
      |    "taxDeducted":     1365.12,
      |    "rentARoom": {
      |        "rentsReceived": 412.89
      |     }
      |}
      |""".stripMargin)

  val downstreamJson: JsValue = Json.parse("""
    |{
    |    "rentIncome": {
    |          "amount":      215.16,
    |          "taxDeducted": 1365.12
    |     },
    |    "ukRentARoom": {
    |          "rentsReceived": 412.89
    |    }
    |   }
    |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[UkFhlPropertyIncome] shouldBe requestBody
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe downstreamJson
      }
    }
  }

}
