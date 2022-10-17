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

package v2.models.request.amendUkPropertyPeriodSummary.amendUkNonFhlProperty

import play.api.libs.json.Json
import support.UnitSpec
import v2.models.request.amendUkPropertyPeriodSummary.AmendUkPropertyPeriodSummaryFixture.{
  amendUkNonFhlProperty,
  downstreamAmendUkNonFhlPropertyJson,
  mtdAmendUkNonFhlPropertyJson
}
import v2.models.utils.JsonErrorValidators

class AmendUkNonFhlPropetySpec extends UnitSpec with JsonErrorValidators {
  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        mtdAmendUkNonFhlPropertyJson.as[AmendUkNonFhlProperty] shouldBe amendUkNonFhlProperty
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(amendUkNonFhlProperty) shouldBe downstreamAmendUkNonFhlPropertyJson
      }
    }
  }
}
