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

package v5.createForeignPropertyPeriodSummary.model.request

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v5.createForeignPropertyPeriodSummary.def1.model.Def1_CreateForeignPropertyPeriodSummaryFixtures

class CreateForeignPropertyPeriodSummaryRequestBodySpec extends UnitSpec with Def1_CreateForeignPropertyPeriodSummaryFixtures {

  "reads" when {
    "passed valid JSON with regular expenses" should {
      "return a valid model" in {
        regularMtdRequestJson.as[Def1_CreateForeignPropertyPeriodSummaryRequestBody] shouldBe regularExpensesRequestBody
      }
    }

    "passed valid JSON with consolidated expenses" should {
      "return a valid model" in {
        consolidatedMtdRequestJson.as[Def1_CreateForeignPropertyPeriodSummaryRequestBody] shouldBe consolidatedExpensesRequestBody
      }
    }
  }

  "writes" when {
    "passed valid model with regular expenses" should {
      "return valid JSON" in {
        Json.toJson(regularExpensesRequestBody) shouldBe regularDownstreamRequestJson
      }
    }

    "passed valid model with consolidated expenses" should {
      "return valid JSON" in {
        Json.toJson(consolidatedExpensesRequestBody) shouldBe consolidatedDownstreamRequestJson
      }
    }
  }

}
