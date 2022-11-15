/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.models.request.amendUkPropertyPeriodSummary

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.models.request.common.ukPropertyRentARoom._
import v2.models.request.common.ukFhlProperty._
import v2.models.request.common.ukNonFhlProperty._
import v2.models.utils.JsonErrorValidators

class AmendUkPropertyPeriodSummaryRequestBodySpec extends UnitSpec with JsonErrorValidators {

  val amendForeignPropertyRequestBody: AmendUkPropertyPeriodSummaryRequestBody =
    AmendUkPropertyPeriodSummaryRequestBody(
      Some(
        UkFhlProperty(
          Some(
            UkFhlPropertyIncome(
              Some(1234.12),
              Some(1234.23),
              Some(UkPropertyIncomeRentARoom(
                Some(1234.34)
              ))
            )),
          Some(UkFhlPropertyExpenses(
            Some(1234.45),
            Some(1234.56),
            Some(1234.67),
            Some(1234.78),
            Some(1234.89),
            Some(1234.12),
            consolidatedExpenses = None,
            Some(1234.23),
            Some(UkPropertyExpensesRentARoom(
              Some(1234.34)
            ))
          ))
        )),
      Some(
        UkNonFhlProperty(
          Some(
            UkNonFhlPropertyIncome(
              Some(9876.12),
              Some(9876.23),
              Some(9876.34),
              Some(9876.45),
              Some(9876.56),
              Some(UkPropertyIncomeRentARoom(
                Some(9876.67)
              ))
            )),
          Some(UkNonFhlPropertyExpenses(
            Some(9876.78),
            Some(9876.89),
            Some(9876.12),
            Some(9876.23),
            Some(9876.34),
            Some(9876.45),
            Some(9876.56),
            Some(9876.67),
            Some(9876.78),
            Some(UkPropertyExpensesRentARoom(
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
        readsJson.as[AmendUkPropertyPeriodSummaryRequestBody] shouldBe amendForeignPropertyRequestBody
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
