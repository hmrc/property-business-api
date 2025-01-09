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

package v4.amendUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v4.amendUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom.Def1_Amend_UkPropertyIncomeRentARoom

class Def1_Amend_UkNonFhlPropertySpec extends UnitSpec {

  private val requestBody =
    Def1_Amend_UkNonFhlProperty(
      Some(
        Def1_Amend_UkNonFhlPropertyIncome(
          Some(41.12),
          Some(84.31),
          Some(9884.93),
          Some(842.99),
          Some(31.44),
          Some(
            Def1_Amend_UkPropertyIncomeRentARoom(
              Some(947.66)
            ))
        )),
      Some(
        Def1_Amend_UkNonFhlPropertyExpenses(
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          Some(988.18)
        ))
    )

  private val mtdJson = Json.parse("""
      |{
      |    "income": {
      |        "premiumsOfLeaseGrant": 41.12,
      |        "reversePremiums": 84.31,
      |        "periodAmount": 9884.93,
      |        "taxDeducted": 842.99,
      |        "otherIncome": 31.44,
      |        "rentARoom": {
      |            "rentsReceived": 947.66
      |        }
      |    },
      |    "expenses": {
      |        "consolidatedExpenses": 988.18
      |    }
      |}
      |""".stripMargin)

  private val downstreamJson = Json.parse("""
      |{
      |    "income": {
      |        "premiumsOfLeaseGrant": 41.12,
      |        "reversePremiums": 84.31,
      |        "periodAmount": 9884.93,
      |        "taxDeducted": 842.99,
      |        "otherIncome": 31.44,
      |        "ukOtherRentARoom": {
      |            "rentsReceived": 947.66
      |        }
      |    },
      |    "expenses": {
      |        "consolidatedExpense": 988.18
      |    }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def1_Amend_UkNonFhlProperty] shouldBe requestBody
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
