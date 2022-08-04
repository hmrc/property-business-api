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

package v2.models.response.createHistoricFhlUkPiePeriodSummary

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec

class CreateHistoricFhlUkPiePeriodSummaryResponseSpec extends UnitSpec {

  val transactionReference: String = "v2509e91f-2689-453e-9ddc-7e3cf97a8e41"

  val createHistoricFhlUkPiePeriodSummaryResponse: CreateHistoricFhlUkPiePeriodSummaryResponse =
    CreateHistoricFhlUkPiePeriodSummaryResponse(transactionReference)

  val json: JsValue = Json.parse(s"""
      |{
      |   "transactionReference": "$transactionReference"
      |}
      """.stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        json.as[CreateHistoricFhlUkPiePeriodSummaryResponse] shouldBe createHistoricFhlUkPiePeriodSummaryResponse
      }
    }
  }
}
