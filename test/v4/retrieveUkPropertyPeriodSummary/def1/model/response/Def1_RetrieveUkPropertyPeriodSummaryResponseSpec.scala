/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.retrieveUkPropertyPeriodSummary.def1.model.response

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v4.retrieveUkPropertyPeriodSummary.def1.model.Def1_RetrieveUkPropertyPeriodSummaryFixture
import v4.retrieveUkPropertyPeriodSummary.model.response.*

class Def1_RetrieveUkPropertyPeriodSummaryResponseSpec extends UnitSpec with Def1_RetrieveUkPropertyPeriodSummaryFixture {

  "RetrieveUkPropertyPeriodSummaryResponse" when {
    "read from downstream JSON" should {
      "create the parsed object" in {
        val result = fullDownstreamJson.as[Def1_RetrieveUkPropertyPeriodSummaryResponse]
        result shouldBe fullResponse
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        val result = Json.toJson(fullResponse)
        result shouldBe fullMtdJson
      }
    }
  }

}
