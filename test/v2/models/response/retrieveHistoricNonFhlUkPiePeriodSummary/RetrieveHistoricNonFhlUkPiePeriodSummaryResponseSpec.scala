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

package v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary

import mocks.MockAppConfig
import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.models.hateoas.{ Link, Method }

class RetrieveHistoricNonFhlUkPiePeriodSummaryResponseSpec extends UnitSpec with MockAppConfig {

  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  val periodIncome: PeriodIncome =
    PeriodIncome(
      decimal("5000.99"),
      decimal("4996.99"),
      decimal("4999.99"),
      decimal("4998.99"),
      decimal("4997.99"),
      Option(RentARoomIncome(Some(4995.99)))
    )

  val periodExpenses: PeriodExpenses =
    PeriodExpenses(
      decimal("5000.99"),
      decimal("4999.99"),
      decimal("4998.99"),
      decimal("4997.99"),
      decimal("4996.99"),
      decimal("4995.99"),
      decimal("4994.99"),
      decimal("4993.99"),
      decimal("4992.99"),
      decimal("4991.99"),
      Option(RentARoomExpenses(Some(4990.99)))
    )

  val readsJson: JsValue = Json.parse("""{
                                             |   "from": "2021-01-01",
                                             |   "to": "2021-02-01",
                                             |   "financials": {
                                             |      "incomes": {
                                     |                 "rentIncome": {
                                             |            "amount": 5000.99,
                                             |            "taxDeducted": 4996.99
                                             |        },
                                             |        "premiumsOfLeaseGrant": 4999.99,
                                             |        "reversePremiums": 4998.99,
                                             |        "otherIncome": 4997.99,
                                             |        "ukRentARoom": {
                                             |            "rentsReceived": 4995.99
                                             |         }
                                             |      },
                                             |      "deductions": {
                                                      "premisesRunningCosts": 5000.99,
                                             |         "repairsAndMaintenance": 4999.99,
                                             |         "financialCosts": 4998.99,
                                             |         "professionalFees": 4997.99,
                                             |         "costOfServices": 4996.99,
                                             |         "other": 4995.99,
                                             |         "travelCosts": 4994.99,
                                             |         "residentialFinancialCost": 4993.99,
                                             |         "residentialFinancialCostsCarriedForward": 4992.99,         
                                             |         "consolidatedExpenses": 4991.99,
                                             |         "ukRentARoom": {
                                             |            "amountClaimed": 4990.99
                                             |         }
                                             |      }
                                             |   }
                                             |}
                                             |""".stripMargin)

  val writesJson: JsValue = Json.parse("""{
                   |      "fromDate": "2021-01-01",
                   |      "toDate": "2021-02-01",             
                   |      "income": {
                   |          "periodAmount": 5000.99,
                   |          "premiumsOfLeaseGrant": 4999.99,
                   |          "reversePremiums": 4998.99,
                   |          "otherIncome": 4997.99,
                   |          "taxDeducted": 4996.99,
                   |          "rentARoom":{
                   |              "rentsReceived": 4995.99
                   |          }
                   |      },
                   |      "expenses": {
                   |          "premisesRunningCosts": 5000.99,
                   |          "repairsAndMaintenance": 4999.99,
                   |          "financialCosts": 4998.99,
                   |          "professionalFees": 4997.99,
                   |          "costOfServices": 4996.99,
                   |          "other": 4995.99,
                   |          "travelCosts": 4994.99,
                   |          "residentialFinancialCost": 4993.99,
                   |          "residentialFinancialCostsCarriedForward": 4992.99,
                   |          "consolidatedExpenses": 4991.99,
                   |          "rentARoom":{
                   |              "amountClaimed":4990.99
                   |          }
                   |      }
                   |}
                   |""".stripMargin)

  val model: RetrieveHistoricNonFhlUkPiePeriodSummaryResponse = RetrieveHistoricNonFhlUkPiePeriodSummaryResponse(
    fromDate = "2021-01-01",
    toDate = "2021-02-01",
    income = Some(
      periodIncome
    ),
    expenses = Some(
      periodExpenses
    )
  )

  "reads" should {
    "read the JSON object from downstream into a case class" in {
      val result = readsJson.as[RetrieveHistoricNonFhlUkPiePeriodSummaryResponse]
      result shouldBe model
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(model) shouldBe writesJson
      }
    }
  }

  "LinksFactory" should {
    "produce the correct links" when {
      "called" in {
        val data: RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData = RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData("myNino", "myPeriodId")

        MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()

        RetrieveHistoricNonFhlUkPiePeriodSummaryResponse.RetrieveNonFhlUkPiePeriodSummaryLinksFactory.links(mockAppConfig, data) shouldBe Seq(
          Link(
            href = s"/my/context/uk/period/non-furnished-holiday-lettings/${data.nino}/${data.periodId}",
            method = Method.PUT,
            rel = "amend-uk-property-historic-non-fhl-period-summary"
          ),
          Link(href = s"/my/context/uk/period/non-furnished-holiday-lettings/${data.nino}/${data.periodId}", method = Method.GET, rel = "self"),
          Link(href = s"/my/context/uk/period/non-furnished-holiday-lettings/${data.nino}",
               method = Method.GET,
               rel = "list-uk-property-historic-non-fhl-period-summaries")
        )
      }
    }
  }

}
