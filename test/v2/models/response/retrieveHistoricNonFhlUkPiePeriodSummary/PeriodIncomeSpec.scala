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

package v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v2.models.utils.JsonErrorValidators

class PeriodIncomeSpec extends UnitSpec with JsonErrorValidators {

  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  val periodIncome: PeriodIncome =
    PeriodIncome(
      periodAmount = decimal("5000.99"),
      premiumsOfLeaseGrant = decimal("4999.99"),
      reversePremiums = decimal("4998.99"),
      otherIncome = decimal("4997.99"),
      taxDeducted = decimal("4996.99"),
      rentARoom = Option(RentARoomIncome(Some(4995.99)))
    )

  val writesJson: JsValue = Json.parse(
    """{
      |    "periodAmount": 5000.99,
      |    "premiumsOfLeaseGrant": 4999.99,
      |    "reversePremiums": 4998.99,
      |    "otherIncome": 4997.99,
      |    "taxDeducted": 4996.99,
      |    "rentARoom":{
      |      "rentsReceived": 4995.99
      |    }
      |  }
      |""".stripMargin
  )

  val readsJson: JsValue = Json.parse(""" {
                                        |        "rentIncome": {
                                        |            "amount": 5000.99,
                                        |            "taxDeducted": 4996.99
                                        |        },
                                        |        "premiumsOfLeaseGrant": 4999.99,
                                        |        "reversePremiums": 4998.99,
                                        |        "otherIncome": 4997.99,
                                        |        "ukRentARoom": {
                                        |            "rentsReceived": 4995.99
                                        |         }
                                        |      }
                                        |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[PeriodIncome] shouldBe periodIncome
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(periodIncome) shouldBe writesJson
      }
    }
  }

}
