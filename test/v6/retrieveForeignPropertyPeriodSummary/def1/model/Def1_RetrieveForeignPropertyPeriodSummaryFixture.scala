/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.retrieveForeignPropertyPeriodSummary.def1.model

import play.api.libs.json.{JsValue, Json}

trait Def1_RetrieveForeignPropertyPeriodSummaryFixture {

  val fullDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2020-06-17T10:53:38.000Z",
      |  "fromDate": "2019-01-29",
      |  "toDate": "2020-03-29",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 1123.89
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": -332.78,
      |      "repairsAndMaintenance": -231.45,
      |      "financialCosts": 345.23,
      |      "professionalFees": -232.45,
      |      "costOfServices": -231.56,
      |      "travelCosts": 234.67,
      |      "other": 3457.9
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "AFG",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 440.31
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 950.48,
      |        "otherPropertyIncome": 802.49,
      |        "foreignTaxPaidOrDeducted": 734.18,
      |        "specialWithholdingTaxOrUkTaxPaid": 85.47
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": -4929.5,
      |        "repairsAndMaintenance": -54.3,
      |        "financialCosts": 2090.35,
      |        "professionalFees": -90.2,
      |        "travelCosts": 560.99,
      |        "costOfServices": -100.83,
      |        "residentialFinancialCost": 857.78,
      |        "broughtFwdResidentialFinancialCost": 600.1,
      |        "other": 334.64
      |      }
      |    }
      |  ],
      |  "deletedOn": "2026-01-13T01:41:53.064Z"
      |}
      """.stripMargin
  )

  val fullMtdJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2020-06-17T10:53:38.000Z",
      |  "fromDate": "2019-01-29",
      |  "toDate": "2020-03-29",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 1123.89
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": -332.78,
      |      "repairsAndMaintenance": -231.45,
      |      "financialCosts": 345.23,
      |      "professionalFees": -232.45,
      |      "costOfServices": -231.56,
      |      "travelCosts": 234.67,
      |      "other": 3457.90
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "AFG",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 440.31
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 950.48,
      |        "otherPropertyIncome": 802.49,
      |        "foreignTaxPaidOrDeducted": 734.18,
      |        "specialWithholdingTaxOrUkTaxPaid": 85.47
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": -4929.50,
      |        "repairsAndMaintenance": -54.30,
      |        "financialCosts": 2090.35,
      |        "professionalFees": -90.20,
      |        "travelCosts": 560.99,
      |        "costOfServices": -100.83,
      |        "residentialFinancialCost": 857.78,
      |        "broughtFwdResidentialFinancialCost": 600.10,
      |        "other": 334.64
      |      }
      |    }
      |  ]
      |}
      """.stripMargin
  )

}
