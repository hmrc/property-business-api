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

package v3.models.response.retrieveHistoricFhlUkPiePeriodSummary

import play.api.libs.json.Json
import shared.utils.UnitSpec

class PeriodIncomeSpec extends UnitSpec {

  private def decimal(value: String): Option[BigDecimal] = Some(BigDecimal(value))

  private val periodIncome =
    PeriodIncome(
      decimal("5000.99"),
      decimal("5000.99"),
      Some(RentARoomIncome(Some(5000.99)))
    )

  private val writesJson = Json.parse(
    """{
      |"periodAmount":5000.99,
      |    "taxDeducted":5000.99,
      |    "rentARoom":{
      |      "rentsReceived":5000.99
      |    }
      |  }
      |""".stripMargin
  )

  private val readsJson = Json.parse(""" 
      |{
      |         "rentIncome": {
      |            "amount": 5000.99,
      |            "taxDeducted": 5000.99
      |         },
      |         "premiumsOfLeaseGrant": 5000.99,
      |         "reversePremiums": 5000.99,
      |         "otherIncome": 5000.99,
      |        "ukRentARoom": {
      |            "rentsReceived": 5000.99
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
