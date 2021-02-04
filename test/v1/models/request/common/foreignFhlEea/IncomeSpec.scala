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

package v1.models.request.common.foreignFhlEea

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class IncomeSpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    s"""
       |{
       |  "rentAmount": 567.83,
       |  "taxDeducted": 4321.92
       |}
       |""".stripMargin)

  val model: ForeignFhlEeaIncome = ForeignFhlEeaIncome(rentAmount = 567.83, taxDeducted = Some(4321.92))

  val desJson: JsValue = Json.parse(
    s"""
       |{
       |  "rentAmount": 567.83,
       |  "taxDeducted": 4321.92
       |}
       |""".stripMargin)


  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[ForeignFhlEeaIncome] shouldBe model
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
