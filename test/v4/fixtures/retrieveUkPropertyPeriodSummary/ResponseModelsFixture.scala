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

package v4.fixtures.retrieveUkPropertyPeriodSummary

import shared.models.domain.Timestamp
import play.api.libs.json.{JsObject, JsValue, Json}
import v4.retrieveUkPropertyPeriodSummary.def1.model.response.*
import v4.retrieveUkPropertyPeriodSummary.model.response.Def1_RetrieveUkPropertyPeriodSummaryResponse

trait ResponseModelsFixture {

  val fullDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2020-06-17T10:53:38.000Z",
      |  "fromDate": "2019-01-29",
      |  "toDate": "2020-03-29",
      |  "periodCreationDate": "2020-06-17T10:53:38Z",
      |  "ukFhlProperty": {
      |    "income": {
      |      "periodAmount": 1.11,
      |      "taxDeducted": 1.12,
      |      "ukFhlRentARoom": {
      |        "rentsReceived": 1.13
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 2.11,
      |      "repairsAndMaintenance": 2.12,
      |      "financialCosts": 2.13 ,
      |      "professionalFees": 2.14,
      |      "costOfServices": 2.15,
      |      "other": 2.16,
      |      "consolidatedExpense": 2.17,
      |      "travelCosts": 2.18,
      |      "ukFhlRentARoom": {
      |        "amountClaimed": 2.19
      |      }
      |    }
      |  },
      |  "ukOtherProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 3.11,
      |      "reversePremiums": 3.12,
      |      "periodAmount": 3.13,
      |      "taxDeducted": 3.14,
      |      "otherIncome": 3.15,
      |      "ukOtherRentARoom": {
      |        "rentsReceived": 3.16
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 4.11,
      |      "repairsAndMaintenance": 4.12,
      |      "financialCosts": 4.13,
      |      "professionalFees": 4.14,
      |      "costOfServices": 4.15,
      |      "other": 4.16,
      |      "consolidatedExpense": 4.17,
      |      "residentialFinancialCost": 4.18,
      |      "travelCosts": 4.19,
      |      "residentialFinancialCostsCarriedForward": 4.20,
      |      "ukOtherRentARoom": {
      |        "amountClaimed": 4.21
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  val fullMtdJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2020-06-17T10:53:38.000Z",
      |  "fromDate": "2019-01-29",
      |  "toDate": "2020-03-29",
      |  "ukFhlProperty": {
      |    "income": {
      |      "periodAmount": 1.11,
      |      "taxDeducted": 1.12,
      |      "rentARoom": {
      |        "rentsReceived": 1.13
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 2.11,
      |      "repairsAndMaintenance": 2.12,
      |      "financialCosts": 2.13 ,
      |      "professionalFees": 2.14,
      |      "costOfServices": 2.15,
      |      "other": 2.16,
      |      "consolidatedExpenses": 2.17,
      |      "travelCosts": 2.18,
      |      "rentARoom": {
      |        "amountClaimed": 2.19
      |      }
      |    }
      |  },
      |  "ukNonFhlProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 3.11,
      |      "reversePremiums": 3.12,
      |      "periodAmount": 3.13,
      |      "taxDeducted": 3.14,
      |      "otherIncome": 3.15,
      |      "rentARoom": {
      |        "rentsReceived": 3.16
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 4.11,
      |      "repairsAndMaintenance": 4.12,
      |      "financialCosts": 4.13,
      |      "professionalFees": 4.14,
      |      "costOfServices": 4.15,
      |      "other": 4.16,
      |      "consolidatedExpenses": 4.17,
      |      "residentialFinancialCost": 4.18,
      |      "travelCosts": 4.19,
      |      "residentialFinancialCostsCarriedForward": 4.20,
      |      "rentARoom": {
      |        "amountClaimed": 4.21
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  val fhlRentARoomIncomeModel: Def1_Retrieve_RentARoomIncome = Def1_Retrieve_RentARoomIncome(
    rentsReceived = Some(1.13)
  )

  val fhlPropertyIncomeModel: Def1_Retrieve_FhlPropertyIncome = Def1_Retrieve_FhlPropertyIncome(
    periodAmount = Some(1.11),
    taxDeducted = Some(1.12),
    rentARoom = Some(fhlRentARoomIncomeModel)
  )

  val fhlRentARoomExpensesModel: Def1_Retrieve_RentARoomExpenses = Def1_Retrieve_RentARoomExpenses(
    amountClaimed = Some(2.19)
  )

  val fhlPropertyExpensesModel: Def1_Retrieve_FhlPropertyExpenses = Def1_Retrieve_FhlPropertyExpenses(
    premisesRunningCosts = Some(2.11),
    repairsAndMaintenance = Some(2.12),
    financialCosts = Some(2.13),
    professionalFees = Some(2.14),
    costOfServices = Some(2.15),
    other = Some(2.16),
    travelCosts = Some(2.18),
    rentARoom = Some(fhlRentARoomExpensesModel),
    consolidatedExpenses = Some(2.17)
  )

  val ukFhlPropertyModel: Def1_Retrieve_UkFhlProperty = Def1_Retrieve_UkFhlProperty(
    income = Some(fhlPropertyIncomeModel),
    expenses = Some(fhlPropertyExpensesModel)
  )

  val ukNonFhlRentARoomIncomeModel: Def1_Retrieve_RentARoomIncome = Def1_Retrieve_RentARoomIncome(
    rentsReceived = Some(3.16)
  )

  val ukNonFhlIncomeModel: Def1_Retrieve_NonFhlPropertyIncome = Def1_Retrieve_NonFhlPropertyIncome(
    premiumsOfLeaseGrant = Some(3.11),
    reversePremiums = Some(3.12),
    periodAmount = Some(3.13),
    taxDeducted = Some(3.14),
    otherIncome = Some(3.15),
    rentARoom = Some(ukNonFhlRentARoomIncomeModel)
  )

  val ukNonFhlRentARoomExpensesModel: Def1_Retrieve_RentARoomExpenses = Def1_Retrieve_RentARoomExpenses(
    amountClaimed = Some(4.21)
  )

  val ukNonFhlExpensesModel: Def1_Retrieve_NonFhlPropertyExpenses = Def1_Retrieve_NonFhlPropertyExpenses(
    premisesRunningCosts = Some(4.11),
    repairsAndMaintenance = Some(4.12),
    financialCosts = Some(4.13),
    professionalFees = Some(4.14),
    costOfServices = Some(4.15),
    other = Some(4.16),
    residentialFinancialCost = Some(4.18),
    travelCosts = Some(4.19),
    residentialFinancialCostsCarriedForward = Some(4.20),
    rentARoom = Some(ukNonFhlRentARoomExpensesModel),
    consolidatedExpenses = Some(4.17)
  )

  val ukNonFhlPropertyModel: Def1_Retrieve_UkNonFhlProperty = Def1_Retrieve_UkNonFhlProperty(
    income = Some(ukNonFhlIncomeModel),
    expenses = Some(ukNonFhlExpensesModel)
  )

  val fullResponseModel: Def1_RetrieveUkPropertyPeriodSummaryResponse = Def1_RetrieveUkPropertyPeriodSummaryResponse(
    submittedOn = Timestamp("2020-06-17T10:53:38.000Z"),
    fromDate = "2019-01-29",
    toDate = "2020-03-29",
    //    periodCreationDate = Some("2020-06-17T10:53:38Z"), // To be reinstated, see MTDSA-15575
    ukFhlProperty = Some(ukFhlPropertyModel),
    ukNonFhlProperty = Some(ukNonFhlPropertyModel)
  )

  val mtdResponseWithHateoas: JsObject = fullMtdJson.as[JsObject] ++ Json
    .parse(
      s"""
         |{
         |   "links":[
         |      {
         |         "href":"/individuals/business/property/AA123456A/XAIS12345678910/period/2022-23/4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
         |         "method":"GET",
         |         "rel":"self"
         |      }
         |   ]
         |}
    """.stripMargin
    )
    .as[JsObject]

}
