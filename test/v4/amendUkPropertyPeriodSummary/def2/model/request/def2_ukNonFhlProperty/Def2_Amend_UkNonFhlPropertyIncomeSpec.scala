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

package v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v4.amendUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom.Def2_Amend_UkPropertyIncomeRentARoom

class Def2_Amend_UkNonFhlPropertyIncomeSpec extends UnitSpec {

  private val requestBody =
    Def2_Amend_UkNonFhlPropertyIncome(
      Some(41.12),
      Some(84.31),
      Some(9884.93),
      Some(842.99),
      Some(31.44),
      Some(
        Def2_Amend_UkPropertyIncomeRentARoom(
          Some(947.66)
        ))
    )

  private val mtdJson = Json.parse("""
      |{
      |    "premiumsOfLeaseGrant": 41.12,
      |    "reversePremiums": 84.31,
      |    "periodAmount": 9884.93,
      |    "taxDeducted": 842.99,
      |    "otherIncome": 31.44,
      |    "rentARoom": {
      |        "rentsReceived": 947.66
      |    }
      |}
      |""".stripMargin)

  private val downstreamJson = Json.parse("""
      |{
      |    "premiumsOfLeaseGrant": 41.12,
      |    "reversePremiums": 84.31,
      |    "periodAmount": 9884.93,
      |    "taxDeducted": 842.99,
      |    "otherIncome": 31.44,
      |    "ukOtherRentARoom": {
      |        "rentsReceived": 947.66
      |    }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def2_Amend_UkNonFhlPropertyIncome] shouldBe requestBody
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
