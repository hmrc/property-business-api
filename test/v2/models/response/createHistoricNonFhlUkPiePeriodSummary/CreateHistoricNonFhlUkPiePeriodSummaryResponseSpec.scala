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

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec

class CreateHistoricNonFhlUkPiePeriodSummaryResponseSpec extends UnitSpec {

  val transactionRef: String = "v2509e91f-2689-453e-9ddc-7e3cf97a8e41"
  val periodId: String       = "2017-04-06_2017-07-05"

  val jsonFromDownstream: JsValue = Json.parse(s"""
                                                  | {
                                                  |     "transactionReference": "$transactionRef"
                                                  | }
      """.stripMargin)

  val expectedJsontoVendor: JsValue = Json.parse(s"""
                                                    | {
                                                    |    "periodId": "$periodId"
                                                    | }
       """.stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid object" in {
        val expected = CreateHistoricNonFhlUkPiePeriodSummaryResponse(transactionRef, None)
        val result   = jsonFromDownstream.as[CreateHistoricNonFhlUkPiePeriodSummaryResponse]
        result shouldBe expected
      }
    }
  }

  "writes" when {
    "passed an object" should {
      "return the object as JSON" in {
        val response = CreateHistoricNonFhlUkPiePeriodSummaryResponse(transactionRef, Some(periodId))
        val result   = Json.toJson(response)
        result shouldBe expectedJsontoVendor
      }
    }
  }
}
