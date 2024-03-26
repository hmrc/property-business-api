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

package v4.controllers.amendUkPropertyPeriodSummary.def1.model.request

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v4.controllers.amendUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty._
import v4.controllers.amendUkPropertyPeriodSummary.def1.model.request.def1_ukNonFhlProperty._
import v4.controllers.amendUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom._
import v4.controllers.amendUkPropertyPeriodSummary.model.request.Def1_AmendUkPropertyPeriodSummaryRequestBody

class AmendUkPropertyPeriodSummaryRequestBodySpec extends UnitSpec {

  val amendForeignPropertyRequestBody: Def1_AmendUkPropertyPeriodSummaryRequestBody =
    Def1_AmendUkPropertyPeriodSummaryRequestBody(
      Some(
        Def1_Amend_UkFhlProperty(
          Some(
            Def1_Amend_UkFhlPropertyIncome(
              Some(1234.12),
              Some(1234.23),
              Some(Def1_Amend_UkPropertyIncomeRentARoom(
                Some(1234.34)
              ))
            )),
          Some(Def1_Amend_UkFhlPropertyExpenses(
            Some(1234.45),
            Some(1234.56),
            Some(1234.67),
            Some(1234.78),
            Some(1234.89),
            Some(1234.12),
            consolidatedExpenses = None,
            Some(1234.23),
            Some(Def1_Amend_UkPropertyExpensesRentARoom(
              Some(1234.34)
            ))
          ))
        )),
      Some(
        Def1_Amend_UkNonFhlProperty(
          Some(
            Def1_Amend_UkNonFhlPropertyIncome(
              Some(9876.12),
              Some(9876.23),
              Some(9876.34),
              Some(9876.45),
              Some(9876.56),
              Some(Def1_Amend_UkPropertyIncomeRentARoom(
                Some(9876.67)
              ))
            )),
          Some(Def1_Amend_UkNonFhlPropertyExpenses(
            Some(9876.78),
            Some(9876.89),
            Some(9876.12),
            Some(9876.23),
            Some(9876.34),
            Some(9876.45),
            Some(9876.56),
            Some(9876.67),
            Some(9876.78),
            Some(Def1_Amend_UkPropertyExpensesRentARoom(
              Some(9876.89)
            )),
            None
          ))
        ))
    )

  val readsJson: JsValue = Json.parse("""{
      |  "ukFhlProperty": {
      |    "income": {
      |      "periodAmount": 1234.12,
      |      "taxDeducted": 1234.23,
      |      "rentARoom": {
      |        "rentsReceived": 1234.34
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 1234.45,
      |      "repairsAndMaintenance": 1234.56,
      |      "financialCosts": 1234.67,
      |      "professionalFees": 1234.78,
      |      "costOfServices": 1234.89,
      |      "other": 1234.12,
      |      "travelCosts": 1234.23,
      |      "rentARoom" : {
      |        "amountClaimed": 1234.34
      |      }
      |    }
      |  },
      |  "ukNonFhlProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 9876.12,
      |      "reversePremiums": 9876.23,
      |      "periodAmount": 9876.34,
      |      "taxDeducted": 9876.45,
      |      "otherIncome": 9876.56,
      |      "rentARoom": {
      |        "rentsReceived": 9876.67
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 9876.78,
      |      "repairsAndMaintenance": 9876.89,
      |      "financialCosts": 9876.12,
      |      "professionalFees": 9876.23,
      |      "costOfServices": 9876.34,
      |      "other": 9876.45,
      |      "residentialFinancialCost": 9876.56,
      |      "travelCosts": 9876.67,
      |      "residentialFinancialCostsCarriedForward": 9876.78,
      |      "rentARoom": {
      |        "amountClaimed": 9876.89
      |      }
      |    }
      |  }
      |}
      |""".stripMargin)

  val writesJson: JsValue = Json.parse("""{
      |  "ukFhlProperty": {
      |    "income": {
      |      "periodAmount": 1234.12,
      |      "taxDeducted": 1234.23,
      |      "ukFhlRentARoom": {
      |        "rentsReceived": 1234.34
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 1234.45,
      |      "repairsAndMaintenance": 1234.56,
      |      "financialCosts": 1234.67,
      |      "professionalFees": 1234.78,
      |      "costOfServices": 1234.89,
      |      "other": 1234.12,
      |      "travelCosts": 1234.23,
      |      "ukFhlRentARoom" : {
      |        "amountClaimed": 1234.34
      |      }
      |    }
      |  },
      |  "ukOtherProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 9876.12,
      |      "reversePremiums": 9876.23,
      |      "periodAmount": 9876.34,
      |      "taxDeducted": 9876.45,
      |      "otherIncome": 9876.56,
      |      "ukOtherRentARoom": {
      |        "rentsReceived": 9876.67
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 9876.78,
      |      "repairsAndMaintenance": 9876.89,
      |      "financialCosts": 9876.12,
      |      "professionalFees": 9876.23,
      |      "costOfServices": 9876.34,
      |      "other": 9876.45,
      |      "residentialFinancialCost": 9876.56,
      |      "travelCosts": 9876.67,
      |      "residentialFinancialCostsCarriedForward": 9876.78,
      |      "ukOtherRentARoom": {
      |        "amountClaimed": 9876.89
      |      }
      |    }
      |  }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[Def1_AmendUkPropertyPeriodSummaryRequestBody] shouldBe amendForeignPropertyRequestBody
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(amendForeignPropertyRequestBody) shouldBe writesJson
      }
    }
  }

}
