/*
 * Copyright 2021 HM Revenue & Customs
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

import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.hateoas.{Link, Method}
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignProperty._
import v2.models.utils.JsonErrorValidators

class RetrieveForeignPropertyPeriodSummaryResponseSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  val retrieveForeignPropertyResponseBody: RetrieveForeignPropertyPeriodSummaryResponse = RetrieveForeignPropertyPeriodSummaryResponse(
    "2020-01-01",
    "2020-01-31",
    Some(ForeignFhlEea(
      ForeignFhlEeaIncome(5000.99),
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
    Some(Seq(ForeignProperty(
      "FRA",
      ForeignPropertyIncome(
        ForeignPropertyRentIncome(5000.99),
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

  val writesJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2020-01-01",
      |  "toDate": "2020-01-31",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
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
      |          "rentAmount": 5000.99
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
    """.stripMargin
  )

  val readsJson: JsValue = Json.parse(
    """
      |{
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
        val data: RetrieveForeignPropertyPeriodSummaryHateoasData = RetrieveForeignPropertyPeriodSummaryHateoasData("myNino", "myBusinessId", "mySubmissionId")

        MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

        RetrieveForeignPropertyPeriodSummaryResponse.RetrieveForeignPropertyLinksFactory.links(mockAppConfig, data) shouldBe Seq(
          Link(href = s"/my/context/${data.nino}/${data.businessId}/period/${data.submissionId}", method = Method.PUT, rel = "amend-property-period-summary"),
          Link(href = s"/my/context/${data.nino}/${data.businessId}/period/${data.submissionId}", method = Method.GET, rel = "self"),
          Link(href = s"/my/context/${data.nino}/${data.businessId}/period", method = Method.GET, rel = "list-property-period-summaries")
        )
      }
    }
  }
}