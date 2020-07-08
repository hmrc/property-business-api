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

package v1.models.response.retrieve.foreignProperty

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.response.retrieveForeignProperty.foreignProperty.ForeignPropertyRentIncome
import v1.models.utils.JsonErrorValidators

class ForeignPropertyRentIncomeSpec extends UnitSpec with JsonErrorValidators {

  val rentIncome = ForeignPropertyRentIncome(
    5000.99,
    Some(5000.99)
  )

  val json = Json.parse(
    """{
      |  "rentAmount": 5000.99,
      |  "taxDeducted": 5000.99
      |}""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        json.as[ForeignPropertyRentIncome] shouldBe rentIncome
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(rentIncome) shouldBe json
      }
    }
  }
}
