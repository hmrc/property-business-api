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

package v5.retrieveUkPropertyCumulativeSummary.def1.model.response

import config.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v5.retrieveUkPropertyCumulativeSummary.def1.model.Def1_RetrieveUkPropertyCumulativeSummaryFixture

class Def1_RetrieveUkPropertyCumulativeSummaryResponseSpec extends UnitSpec with MockAppConfig with Def1_RetrieveUkPropertyCumulativeSummaryFixture {

  "RetrieveUkPropertyCumulativeSummaryResponse" when {
    "read from downstream JSON" should {
      "return the parsed object" in {
        fullDownstreamJson.as[Def1_RetrieveUkPropertyCumulativeSummaryResponse] shouldBe fullResponse
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(fullResponse) shouldBe fullMtdJson
      }
    }
  }

}
