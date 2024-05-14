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

package v4.retrieveUkPropertyPeriodSummary.def2.model

import api.models.domain.Timestamp
import play.api.libs.json.{JsValue, Json}
import v4.retrieveUkPropertyPeriodSummary.def2.model.response._
import v4.retrieveUkPropertyPeriodSummary.model.response._

trait Def2_RetrieveUkPropertyPeriodSummaryConsolidatedFixture {

  val fullDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2025-06-17T10:53:38.000Z",
      |  "fromDate": "2024-01-29",
      |  "toDate": "2025-03-29",
      |  "periodCreationDate": "2025-06-17T10:53:38Z",
      |  "ukFhlProperty": {
      |    "income": {
      |      "periodAmount": 1.11,
      |      "taxDeducted": 1.12,
      |      "ukFhlRentARoom": {
      |        "rentsReceived": 1.13
      |      }
      |    },
      |    "expenses": {
      |      "consolidatedExpense": 2.17,
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
      |      "consolidatedExpense": 4.17,
      |      "residentialFinancialCost": 4.18,
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
      |  "submittedOn": "2025-06-17T10:53:38.000Z",
      |  "fromDate": "2024-01-29",
      |  "toDate": "2025-03-29",
      |  "ukFhlProperty": {
      |    "income": {
      |      "periodAmount": 1.11,
      |      "taxDeducted": 1.12,
      |      "rentARoom": {
      |        "rentsReceived": 1.13
      |      }
      |    },
      |    "expenses": {
      |      "rentARoom": {
      |        "amountClaimed": 2.19
      |      },
      |      "consolidatedExpenses": 2.17
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
      |      "residentialFinancialCost": 4.18,
      |      "residentialFinancialCostsCarriedForward": 4.20,
      |      "rentARoom": {
      |        "amountClaimed": 4.21
      |      },
      |      "consolidatedExpenses": 4.17
      |    }
      |  }
      |}
    """.stripMargin
  )

  val fhlRentARoomIncome: Def2_Retrieve_RentARoomIncome = Def2_Retrieve_RentARoomIncome(
    rentsReceived = Some(1.13)
  )

  val fhlPropertyIncome: Def2_Retrieve_FhlPropertyIncome = Def2_Retrieve_FhlPropertyIncome(
    periodAmount = Some(1.11),
    taxDeducted = Some(1.12),
    rentARoom = Some(fhlRentARoomIncome)
  )

  val fhlRentARoomExpenses: Def2_Retrieve_RentARoomExpenses = Def2_Retrieve_RentARoomExpenses(
    amountClaimed = Some(2.19)
  )

  val fhlPropertyExpenses: Def2_Retrieve_FhlPropertyConsolidatedExpenses = Def2_Retrieve_FhlPropertyConsolidatedExpenses(
    rentARoom = Some(fhlRentARoomExpenses),
    consolidatedExpenses = Some(2.17)
  )

  val ukFhlProperty: Def2_Retrieve_ConsolidatedUkFhlProperty = Def2_Retrieve_ConsolidatedUkFhlProperty(
    income = Some(fhlPropertyIncome),
    expenses = Some(fhlPropertyExpenses)
  )

  val ukNonFhlRentARoomIncome: Def2_Retrieve_RentARoomIncome = Def2_Retrieve_RentARoomIncome(
    rentsReceived = Some(3.16)
  )

  val ukNonFhlIncome: Def2_Retrieve_NonFhlPropertyIncome = Def2_Retrieve_NonFhlPropertyIncome(
    premiumsOfLeaseGrant = Some(3.11),
    reversePremiums = Some(3.12),
    periodAmount = Some(3.13),
    taxDeducted = Some(3.14),
    otherIncome = Some(3.15),
    rentARoom = Some(ukNonFhlRentARoomIncome)
  )

  val ukNonFhlRentARoomExpenses: Def2_Retrieve_RentARoomExpenses = Def2_Retrieve_RentARoomExpenses(
    amountClaimed = Some(4.21)
  )

  val ukNonFhlExpenses: Def2_Retrieve_NonFhlPropertyConsolidatedExpenses = Def2_Retrieve_NonFhlPropertyConsolidatedExpenses(
    residentialFinancialCost = Some(4.18),
    residentialFinancialCostsCarriedForward = Some(4.20),
    rentARoom = Some(ukNonFhlRentARoomExpenses),
    consolidatedExpenses = Some(4.17)
  )

  val ukNonFhlProperty: Def2_Retrieve_ConsolidatedUkNonFhlProperty = Def2_Retrieve_ConsolidatedUkNonFhlProperty(
    income = Some(ukNonFhlIncome),
    expenses = Some(ukNonFhlExpenses)
  )

  val fullResponse: Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse = Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse(
    submittedOn = Timestamp("2025-06-17T10:53:38.000Z"),
    fromDate = "2024-01-29",
    toDate = "2025-03-29",
    //    periodCreationDate = Some("2020-06-17T10:53:38Z"), // To be reinstated, see MTDSA-15575
    ukFhlProperty = Some(ukFhlProperty),
    ukNonFhlProperty = Some(ukNonFhlProperty)
  )

}
