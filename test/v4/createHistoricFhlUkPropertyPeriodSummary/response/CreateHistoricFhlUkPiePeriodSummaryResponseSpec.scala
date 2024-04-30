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

package v4.createHistoricFhlUkPropertyPeriodSummary.response

import play.api.libs.json.{JsObject, JsValue, Json}
import support.UnitSpec
import v4.createHistoricFhlUkPropertyPeriodSummary.model.response.CreateHistoricFhlUkPiePeriodSummaryResponse

class CreateHistoricFhlUkPiePeriodSummaryResponseSpec extends UnitSpec {

  private val model = CreateHistoricFhlUkPiePeriodSummaryResponse(
    periodId = "0000000000000001"
  )

  val desJson: JsValue = Json.parse("""{
      |  "periodId": "0000000000000001"
      |}""".stripMargin)

  val mtdJson: JsObject = Json.obj()

  "reads" should {
    "return a valid model" in {
      desJson.as[CreateHistoricFhlUkPiePeriodSummaryResponse] shouldBe model
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

}
