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

package v2.models.response.retrieveForeignPropertyPeriodSummary

import api.hateoas.{Link, Method}
import api.models.domain.Timestamp
import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignNonFhlProperty._

class RetrieveForeignPropertyPeriodSummaryResponseSpec extends UnitSpec with MockAppConfig {

  private val retrieveForeignPropertyResponseBody = RetrieveForeignPropertyPeriodSummaryResponse(
    Timestamp("2021-06-17T10:53:38Z"),
    "2020-01-01",
    "2020-01-31",
    Some(
      ForeignFhlEea(
        Some(ForeignFhlEeaIncome(Some(5000.99))),
        Some(
          ForeignFhlEeaExpenses(
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
    Some(
      List(ForeignNonFhlProperty(
        "FRA",
        Some(
          ForeignNonFhlPropertyIncome(
            Some(ForeignNonFhlPropertyRentIncome(Some(5000.99))),
            foreignTaxCreditRelief = false,
            Some(5000.99),
            Some(5000.99),
            Some(5000.99),
            Some(5000.99)
          )),
        Some(
          ForeignNonFhlPropertyExpenses(
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
          ))
      )))
  )

  private val writesJson = Json.parse("""
      |{
      |  "submittedOn": "2021-06-17T10:53:38.000Z",
      |  "fromDate": "2020-01-01",
      |  "toDate": "2020-01-31",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99,
      |      "consolidatedExpenses": 5000.99
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxPaidOrDeducted": 5000.99,
      |        "specialWithholdingTaxOrUkTaxPaid": 5000.99
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99,
      |        "consolidatedExpenses": 5000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin)

  private val readsJson = Json.parse(
    """
      |{
      |  "submittedOn": "2021-06-17T10:53:38.000Z",
      |  "fromDate": "2020-01-01",
      |  "toDate": "2020-01-31",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99,
      |      "consolidatedExpense": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxPaidOrDeducted": 5000.99,
      |        "specialWithholdingTaxOrUkTaxPaid": 5000.99
      |      },
      |      "expenses": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99,
      |        "consolidatedExpense": 5000.99
      |      }
      |    }
      |  ]
      |}
    """.stripMargin
  )

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
        val data: RetrieveForeignPropertyPeriodSummaryHateoasData =
          RetrieveForeignPropertyPeriodSummaryHateoasData("myNino", "myBusinessId", "myTaxYear", "mySubmissionId")

        MockedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

        RetrieveForeignPropertyPeriodSummaryResponse.RetrieveForeignPropertyLinksFactory.links(mockAppConfig, data) shouldBe List(
          Link(
            href = s"/my/context/foreign/${data.nino}/${data.businessId}/period/${data.taxYear}/${data.submissionId}",
            method = Method.PUT,
            rel = "amend-foreign-property-period-summary"
          ),
          Link(
            href = s"/my/context/foreign/${data.nino}/${data.businessId}/period/${data.taxYear}/${data.submissionId}",
            method = Method.GET,
            rel = "self"),
          Link(
            href = s"/my/context/${data.nino}/${data.businessId}/period/${data.taxYear}",
            method = Method.GET,
            rel = "list-property-period-summaries")
        )
      }
    }
  }

}
