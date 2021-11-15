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

package v1.models.response.retrieveForeignPropertyPeriodSummary.foreignProperty

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class ForeignPropertyIncomeSpec extends UnitSpec with JsonErrorValidators {

  val foreignPropertyIncome = ForeignPropertyIncome(
    Some(ForeignPropertyRentIncome(Some(5000.99))),
    false,
    Some(5000.99),
    Some(5000.99),
    Some(5000.99),
    Some(5000.99)
  )

  val writesJson = Json.parse(
    """{
      |    "rentIncome": {
      |      "rentAmount": 5000.99
      |    },
      |    "foreignTaxCreditRelief": false,
      |    "premiumOfLeaseGrant": 5000.99,
      |    "otherPropertyIncome": 5000.99,
      |    "foreignTaxTakenOff": 5000.99,
      |    "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |  }""".stripMargin)

  val readsJson = Json.parse(
    """{
      |    "rentIncome": {
      |      "rentAmount": 5000.99
      |    },
      |    "foreignTaxCreditRelief": false,
      |    "premiumsOfLeaseGrant": 5000.99,
      |    "otherPropertyIncome": 5000.99,
      |    "foreignTaxPaidOrDeducted": 5000.99,
      |    "specialWithholdingTaxOrUkTaxPaid": 5000.99
      |  }""".stripMargin)


  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[ForeignPropertyIncome] shouldBe foreignPropertyIncome
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignPropertyIncome) shouldBe writesJson
      }
    }
  }
}
