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

package v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission

import play.api.libs.json.{JsObject, JsValue, Json}
import support.UnitSpec

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponseSpec extends UnitSpec {

  private val model = CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(
    transactionReference = Some("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")
  )

  val desJson: JsValue = Json.parse(
    """{
      |  "transactionReference": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
      |}""".stripMargin)

  val mtdJson: JsObject = Json.obj()

  "reads" should {
    "return a valid model" in {
      desJson.as[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse] shouldBe model
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
