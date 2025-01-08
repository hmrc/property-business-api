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

package v4.historicNonFhlUkPropertyPeriodSummary.amend.model.request

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v4.historicNonFhlUkPropertyPeriodSummary.amend.def1.model.request.Def1_Fixtures

class AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBodySpec extends UnitSpec with Def1_Fixtures {

  "reads" when {
    "passed a valid JSON with full data" should {
      "return a valid model" in {
        mtdJsonRequestFull.as[Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody] shouldBe requestBodyFull
      }
    }

    "passed a valid JSON with consolidated data" should {
      "return a valid model" in {
        mtdJsonRequestConsolidated.as[Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody] shouldBe requestBodyConsolidated
      }
    }
  }

  "writes" when {
    "passed valid model with full data" should {
      "return valid JSON" in {
        Json.toJson(requestBodyFull) shouldBe downstreamJsonRequestFull
      }
    }

    "passed valid model with consolidated data" should {
      "return valid JSON" in {
        Json.toJson(requestBodyConsolidated) shouldBe downstreamJsonRequestConsolidated
      }
    }
  }

}
