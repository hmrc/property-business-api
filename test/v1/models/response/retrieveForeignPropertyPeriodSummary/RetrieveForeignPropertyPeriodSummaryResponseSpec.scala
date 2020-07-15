/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.retrieveForeignPropertyPeriodSummary

import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.hateoas.{Link, Method}
import v1.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
import v1.models.response.retrieveForeignPropertyPeriodSummary.foreignProperty._
import v1.models.utils.JsonErrorValidators

class RetrieveForeignPropertyPeriodSummaryResponseSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  val retrieveForeignPropertyResponseBody = RetrieveForeignPropertyPeriodSummaryResponse(
    "2020-01-01",
    "2020-01-31",
    Some(ForeignFhlEea(
      ForeignFhlEeaIncome(5000.99, Some(5000.99)),
      Some(ForeignFhlEeaExpenditure(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ))
    )),
    Some(Seq(ForeignProperty("FRA",
      ForeignPropertyIncome(
        ForeignPropertyRentIncome(5000.99, Some(5000.99)),
        false,
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ),
      Some(ForeignPropertyExpenditure(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ))))
    ))

  val writesJson = Json.parse(
    """{
      |  "fromDate": "2020-01-01",
      |  "toDate": "2020-01-31",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99,
      |      "taxDeducted": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99,
      |      "consolidatedExpenses": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99,
      |          "taxDeducted": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costsOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99,
      |        "consolidatedExpenses": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val readsJson = Json.parse(
    """{
      |  "fromDate": "2020-01-01",
      |  "toDate": "2020-01-31",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99,
      |      "taxDeducted": 5000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCostsAmount": 5000.99,
      |      "repairsAndMaintenanceAmount": 5000.99,
      |      "financialCostsAmount": 5000.99,
      |      "professionalFeesAmount": 5000.99,
      |      "costOfServicesAmount": 5000.99,
      |      "travelCostsAmount": 5000.99,
      |      "otherAmount": 5000.99,
      |      "consolidatedExpensesAmount": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99,
      |          "taxDeducted": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrantAmount": 5000.99,
      |        "otherPropertyIncomeAmount": 5000.99,
      |        "foreignTaxPaidOrDeducted": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenses": {
      |        "premisesRunningCostsAmount": 5000.99,
      |        "repairsAndMaintenanceAmount": 5000.99,
      |        "financialCostsAmount": 5000.99,
      |        "professionalFeesAmount": 5000.99,
      |        "costOfServicesAmount": 5000.99,
      |        "travelCostsAmount": 5000.99,
      |        "residentialFinancialCostAmount": 5000.99,
      |        "broughtFwdResidentialFinancialCostAmount": 5000.99,
      |        "otherAmount": 5000.99,
      |        "consolidatedExpensesAmount": 5000.99
      |      }
      |    }
      |  ]
      |}""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[RetrieveForeignPropertyPeriodSummaryResponse] shouldBe retrieveForeignPropertyResponseBody
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(retrieveForeignPropertyResponseBody) shouldBe writesJson
      }
    }
  }

  "LinksFactory" should {
    "produce the correct links" when {
      "called" in {
        val data: RetrieveForeignPropertyHateoasData = RetrieveForeignPropertyHateoasData("myNino", "myBusinessId", "mySubmissionId")

        MockedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

        RetrieveForeignPropertyPeriodSummaryResponse.RetrieveForeignPropertyPeriodSummaryLinksFactory.links(mockAppConfig, data) shouldBe Seq(
          Link(href = s"/my/context/${data.nino}/${data.businessId}/period/${data.submissionId}", method = Method.PUT, rel = "amend-property-period-summary"),
          Link(href = s"/my/context/${data.nino}/${data.businessId}/period/${data.submissionId}", method = Method.GET, rel = "self"),
          Link(href = s"/my/context/${data.nino}/${data.businessId}/period", method = Method.GET, rel = "list-property-period-summaries")
        )
      }
    }
  }
}
