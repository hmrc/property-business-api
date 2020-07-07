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

package v1.models.request.amendForeignProperty.foreignPropertyEntry

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class RentIncomeSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    s"""
       |{
       |  "rentAmount": 34456.30,
       |  "taxDeducted": 6334.34
       |}
       |""".stripMargin)

  val model: RentIncome = RentIncome(rentAmount = 34456.30, taxDeducted = 6334.34)

  val desJson: JsValue = Json.parse(
    s"""
       |{
       |  "rentAmount": 34456.30,
       |  "taxDeducted": 6334.34
       |}
       |""".stripMargin)


  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[RentIncome] shouldBe model
      }
    }
  }

  "writes" should {
    "write to JSON" when {
      "valid model is provided" in {
        Json.toJson(model) shouldBe desJson
      }
    }
  }
}
