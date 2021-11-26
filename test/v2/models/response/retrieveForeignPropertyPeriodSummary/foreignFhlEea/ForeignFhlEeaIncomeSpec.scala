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

package v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea

import play.api.libs.json.Json
import support.UnitSpec
import v2.models.utils.JsonErrorValidators

class ForeignFhlEeaIncomeSpec extends UnitSpec with JsonErrorValidators {

  val foreignFhlEeaIncome = ForeignFhlEeaIncome(Some(5000.99))

  val json = Json.parse(
    """{
      |  "rentAmount": 5000.99
      |}""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        json.as[ForeignFhlEeaIncome] shouldBe foreignFhlEeaIncome
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignFhlEeaIncome) shouldBe json
      }
    }
  }
}
