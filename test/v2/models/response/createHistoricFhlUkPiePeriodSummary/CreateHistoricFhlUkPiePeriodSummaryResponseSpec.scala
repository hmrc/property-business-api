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

package v2.models.response.createHistoricFhlUkPiePeriodSummary

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v2.models.domain.PeriodId

class CreateHistoricFhlUkPiePeriodSummaryResponseSpec extends UnitSpec {

  val periodId: String = "2017-04-06_2017-07-05"

  val expectedJsontoVendor: JsValue = Json.parse(s"""
       | {
       |    "periodId": "$periodId"
       | }
       """.stripMargin)

  "writes" when {
    "passed an object" should {
      "return the object as JSON" in {
        val response = CreateHistoricFhlUkPiePeriodSummaryResponse(PeriodId(periodId))
        val result   = Json.toJson(response)
        result shouldBe expectedJsontoVendor
      }
    }
  }
}
