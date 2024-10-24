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

package v5.createAmendUkPropertyCumulativeSummary.def1

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v5.createAmendUkPropertyCumulativeSummary.def1.model.request._

class Def1_CreateAmendAmendUkPropertyCumulativeSummaryRequestBodySpec extends UnitSpec {

  val ukPropertyJson: JsValue = Json.parse(
    """
      |{
      |  "income": {
      |    "premiumsOfLeaseGrant": 42.12,
      |    "reversePremiums": 84.31,
      |    "periodAmount": 9884.93,
      |    "taxDeducted": 842.99,
      |    "otherIncome": 31.44,
      |    "ukOtherRentARoom": {
      |       "rentsReceived": 947.66
      |    }
      |  },
      |  "expenses": {
      |    "residentialFinancialCost": 9000.10,
      |    "residentialFinancialCostsCarriedForward": 300.13,
      |    "ukOtherRentARoom": {
      |       "amountClaimed": 860.88
      |    },
      |    "consolidatedExpenses": -988.18
      |  }
      |}
      |""".stripMargin
  )

  val ukProperty: UkProperty = UkProperty(
    income = Some(
      Income(
        premiumsOfLeaseGrant = Some(42.12),
        reversePremiums = Some(84.31),
        periodAmount = Some(9884.93),
        taxDeducted = Some(842.99),
        otherIncome = Some(31.44),
        rentARoom = Some(RentARoomIncome(rentsReceived = Some(947.66)))
      )
    ),
    expenses = Some(
      Expenses(
        premisesRunningCosts = None,
        repairsAndMaintenance = None,
        financialCosts = None,
        professionalFees = None,
        costOfServices = None,
        other = None,
        residentialFinancialCost = Some(9000.10),
        travelCosts = None,
        residentialFinancialCostsCarriedForward = Some(300.13),
        rentARoom = Some(RentARoomExpenses(amountClaimed = Some(860.88))),
        consolidatedExpenses = Some(-988.18)
      )
    )
  )

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2023-04-01",
      |  "toDate": "2024-04-01",
      |  "ukProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 42.12,
      |      "reversePremiums": 84.31,
      |      "periodAmount": 9884.93,
      |      "taxDeducted": 842.99,
      |      "otherIncome": 31.44,
      |      "rentARoom": {
      |         "rentsReceived": 947.66
      |      }
      |    },
      |    "expenses": {
      |      "residentialFinancialCost": 9000.10,
      |      "residentialFinancialCostsCarriedForward": 300.13,
      |      "rentARoom": {
      |         "amountClaimed": 860.88
      |      },
      |      "consolidatedExpenses": -988.18
      |    }
      |  }
      |}
      |""".stripMargin
  )

  val requestBodyMtdJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2023-04-01",
      |  "toDate": "2024-04-01",
      |  "ukOtherProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 42.12,
      |      "reversePremiums": 84.31,
      |      "periodAmount": 9884.93,
      |      "taxDeducted": 842.99,
      |      "otherIncome": 31.44,
      |      "ukOtherRentARoom": {
      |         "rentsReceived": 947.66
      |      }
      |    },
      |    "expenses": {
      |      "residentialFinancialCostAmount": 9000.10,
      |      "broughtFwdResidentialFinancialCostAmount": 300.13,
      |      "ukOtherRentARoom": {
      |         "amountClaimed": 860.88
      |      },
      |      "consolidatedExpenses": -988.18
      |    }
      |  }
      |}
      |""".stripMargin
  )

  val createAmendAmendUkPropertyCumulativeSummaryRequestBody: Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody =
    Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody(
      fromDate = Some("2023-04-01"),
      toDate = Some("2024-04-01"),
      ukProperty = ukProperty
    )

  "Def1_CreateAmendAmendUkPropertyCumulativeSummaryRequestBody" should {

    "deserialize from JSON correctly" in {
      requestBodyJson.as[Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody] shouldBe createAmendAmendUkPropertyCumulativeSummaryRequestBody
    }

    "serialize to JSON correctly" in {
      Json.toJson(createAmendAmendUkPropertyCumulativeSummaryRequestBody) shouldBe requestBodyMtdJson
    }

  }

}
