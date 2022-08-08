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

package v2.models.response.createHistoricNonFhlUkPiePeriodSummary

import play.api.libs.json.Json
import support.UnitSpec
import v2.models.utils.JsonErrorValidators

class CreateHistoricNonFhlUkPiePeriodSummaryResponseSpec extends UnitSpec with JsonErrorValidators {

  val createHistoricNonFhlUkPiePeriodSummaryResponse = CreateHistoricNonFhlUkPiePeriodSummaryResponse(
    transactionReference = "0000000000000001"
  )

  val json = Json.parse(
    """{
      |  "transactionReference": "0000000000000001"
      |}""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        json.as[CreateHistoricNonFhlUkPiePeriodSummaryResponse] shouldBe createHistoricNonFhlUkPiePeriodSummaryResponse
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(createHistoricNonFhlUkPiePeriodSummaryResponse) shouldBe json
      }
    }
  }
}