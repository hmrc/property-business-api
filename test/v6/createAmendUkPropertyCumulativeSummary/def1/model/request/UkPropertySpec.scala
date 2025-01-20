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

package v6.createAmendUkPropertyCumulativeSummary.def1.model.request

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class UkPropertySpec extends UnitSpec {

  val ukPropertyJson: JsValue = Json.parse("""|
    |{
    |  "income": {
    |    "premiumsOfLeaseGrant": 42.12,
    |    "reversePremiums": 84.31,
    |    "periodAmount": 9884.93,
    |    "taxDeducted": 842.99,
    |    "otherIncome": 31.44,
    |    "rentARoom": {
    |      "rentsReceived": 947.66
    |    }
    |  },
    |  "expenses": {
    |    "consolidatedExpenses": -988.18,
    |    "residentialFinancialCost": 9000.10,
    |    "residentialFinancialCostsCarriedForward": 300.13,
    |    "rentARoom": {
    |      "amountClaimed": 860.88
    |    }
    |  }
    |}
    |""".stripMargin)

  val ukPropertyMtdJson: JsValue = Json.parse(
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
      |    "residentialFinancialCostAmount": 9000.10,
      |    "broughtFwdResidentialFinancialCostAmount": 300.13,
      |    "ukOtherRentARoom": {
      |       "amountClaimed": 860.88
      |    },
      |    "consolidatedExpenses": -988.18
      |  }
      |}
    """.stripMargin
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

  "UkProperty" should {
    "read from json" in {
      ukPropertyJson.as[UkProperty] shouldBe ukProperty
    }

    "write from json" in {
      Json.toJson(ukProperty) shouldBe ukPropertyMtdJson
    }

  }

}
