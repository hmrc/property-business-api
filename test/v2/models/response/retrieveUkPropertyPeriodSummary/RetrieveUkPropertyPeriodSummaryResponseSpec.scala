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

package v2.models.response.retrieveUkPropertyPeriodSummary

import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.hateoas.HateoasFactory
import v2.models.hateoas.HateoasWrapper
import v2.models.hateoas.Link
import v2.models.hateoas.Method._

class RetrieveUkPropertyPeriodSummaryResponseSpec extends UnitSpec with MockAppConfig {

  val downstreamJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2020-06-17T10:53:38Z",
      |  "fromDate": "2019-01-29",
      |  "toDate": "2020-03-29",
      |  "ukFhlProperty": {
      |    "income": {
      |      "periodAmount": 0,
      |      "taxDeducted": 0,
      |      "ukFhlRentARoom": {
      |        "rentsReceived": 0
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 0,
      |      "repairsAndMaintenance": 0,
      |      "financialCosts": 0,
      |      "professionalFees": 0,
      |      "costOfServices": 0,
      |      "other": 0,
      |      "consolidatedExpenses": 0,
      |      "travelCosts": 0,
      |      "ukFhlRentARoom": {
      |        "amountClaimed": 0
      |      }
      |    }
      |  },
      |  "ukOtherProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 0,
      |      "reversePremiums": 0,
      |      "periodAmount": 0,
      |      "taxDeducted": 0,
      |      "otherIncome": 0,
      |      "ukOtherRentARoom": {
      |        "rentsReceived": 0
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 0,
      |      "repairsAndMaintenance": 0,
      |      "financialCosts": 0,
      |      "professionalFees": 0,
      |      "costOfServices": 0,
      |      "other": 0,
      |      "consolidatedExpenses": 0,
      |      "residentialFinancialCost": 0,
      |      "travelCosts": 0,
      |      "residentialFinancialCostsCarriedForward": 0,
      |      "ukOtherRentARoom": {
      |        "amountClaimed": 0
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2020-06-17T10:53:38Z",
      |  "fromDate": "2019-01-29",
      |  "toDate": "2020-03-29",
      |  "ukFhlProperty": {
      |    "income": {
      |      "periodAmount": 0,
      |      "taxDeducted": 0,
      |      "rentARoom": {
      |        "rentsReceived": 0
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 0,
      |      "repairsAndMaintenance": 0,
      |      "financialCosts": 0,
      |      "professionalFees": 0,
      |      "costOfServices": 0,
      |      "other": 0,
      |      "consolidatedExpenses": 0,
      |      "travelCosts": 0,
      |      "rentARoom": {
      |        "amountClaimed": 0
      |      }
      |    }
      |  },
      |  "ukOtherProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 0,
      |      "reversePremiums": 0,
      |      "periodAmount": 0,
      |      "taxDeducted": 0,
      |      "otherIncome": 0,
      |      "rentARoom": {
      |        "rentsReceived": 0
      |      }
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 0,
      |      "repairsAndMaintenance": 0,
      |      "financialCosts": 0,
      |      "professionalFees": 0,
      |      "costOfServices": 0,
      |      "other": 0,
      |      "consolidatedExpenses": 0,
      |      "residentialFinancialCost": 0,
      |      "travelCosts": 0,
      |      "residentialFinancialCostsCarriedForward": 0,
      |      "rentARoom": {
      |        "amountClaimed": 0
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  )

  val model: RetrieveUkPropertyPeriodSummaryResponse = RetrieveUkPropertyPeriodSummaryResponse("A")

  val hateoasData: RetrieveUkPropertyPeriodSummaryHateoasData = RetrieveUkPropertyPeriodSummaryHateoasData(
    nino = "AA999999A",
    businessId = "XAIS12345678910",
    taxYear = "2022-23",
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  "RetrieveUkPropertyPeriodSummaryResponse" when {
    "read from downstream JSON" should {
      "create the expected model" in {
        downstreamJson.as[RetrieveUkPropertyPeriodSummaryResponse] shouldBe model
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

  "hateoasLinksFactory" when {
    "wrap" should {
      "return the expected wrapped response with correct links" in {
        MockAppConfig.apiGatewayContext.returns("individuals/business/property").anyNumberOfTimes()
        val wrappedResponse: HateoasWrapper[RetrieveUkPropertyPeriodSummaryResponse] = new HateoasFactory(mockAppConfig).wrap(model, hateoasData)

        val baseUrl = "/individuals/business/property/uk/AA999999A/XAIS12345678910/period/2022-23"

        val expectedWrappedResponse: HateoasWrapper[RetrieveUkPropertyPeriodSummaryResponse] = HateoasWrapper(
          model,
          Seq(
            Link(s"$baseUrl/4557ecb5-fd32-48cc-81f5-e6acd1099f3c", PUT, "amend-property-period-summary"),
            Link(s"$baseUrl/4557ecb5-fd32-48cc-81f5-e6acd1099f3c", GET, "self"),
            Link("/individuals/business/property/AA999999A/XAIS12345678910/period/2022-23", GET, "list-property-period-summaries")
          )
        )

        wrappedResponse shouldBe expectedWrappedResponse
      }
    }
  }
}
