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
import v2.hateoas.HateoasFactory
import v2.models.hateoas.Method.{GET, PUT}
import v2.models.hateoas.{HateoasWrapper, Link, Method}
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignNonFhlProperty._
import v2.models.utils.JsonErrorValidators

class RetrieveForeignPropertyPeriodSummaryResponseSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  val retrieveForeignPropertyResponseBody: RetrieveForeignPropertyPeriodSummaryResponse = RetrieveForeignPropertyPeriodSummaryResponse(
    "2021-06-17T10:53:38Z",
    "2020-01-01",
    "2020-01-31",
    Some(ForeignFhlEea(
      Some(ForeignFhlEeaIncome(Some(5000.99))),
      Some(ForeignFhlEeaExpenses(
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
    Some(Seq(ForeignNonFhlProperty(
      "FRA",
      Some(ForeignNonFhlPropertyIncome(
        Some(ForeignNonFhlPropertyRentIncome(Some(5000.99))),
        false,
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      )),
      Some(ForeignNonFhlPropertyExpenses(
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
      |  "submittedOn": "2021-06-17T10:53:38Z",
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
    """.stripMargin
  )

  val hateoasData: RetrieveForeignPropertyPeriodSummaryHateoasData = RetrieveForeignPropertyPeriodSummaryHateoasData(
    nino = "AA999999A",
    businessId = "XAIS12345678910",
    taxYear = "2022-23",
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  val readsJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2021-06-17T10:53:38Z",
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

  "hateoasLinksFactory" when {
    "wrap" should {
      "return the expected wrapped response with correct links" in {
        MockAppConfig.apiGatewayContext.returns("individuals/business/property").anyNumberOfTimes()
        val wrappedResponse: HateoasWrapper[RetrieveForeignPropertyPeriodSummaryResponse] = new HateoasFactory(mockAppConfig).wrap(retrieveForeignPropertyResponseBody, hateoasData)

        val baseUrl = "/individuals/business/property/foreign/AA999999A/XAIS12345678910/period/2022-23"

        val expectedWrappedResponse: HateoasWrapper[RetrieveForeignPropertyPeriodSummaryResponse] = HateoasWrapper(
          retrieveForeignPropertyResponseBody,
          Seq(
            Link(s"$baseUrl/4557ecb5-fd32-48cc-81f5-e6acd1099f3c", PUT, "amend-foreign-property-period-summary"),
            Link(s"$baseUrl/4557ecb5-fd32-48cc-81f5-e6acd1099f3c", GET, "self"),
            Link("/individuals/business/property/AA999999A/XAIS12345678910/period/2022-23", GET, "list-property-period-summaries")
          )
        )

        wrappedResponse shouldBe expectedWrappedResponse
      }
    }
  }
}