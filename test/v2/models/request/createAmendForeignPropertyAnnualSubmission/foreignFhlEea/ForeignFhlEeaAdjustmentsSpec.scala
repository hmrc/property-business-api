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

package v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignFhlEea

import play.api.libs.json.Json
import support.UnitSpec
import v2.models.request.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionFixture
import v2.models.utils.JsonErrorValidators

class ForeignFhlEeaAdjustmentsSpec extends UnitSpec with JsonErrorValidators with CreateAmendForeignPropertyAnnualSubmissionFixture {

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        foreignFhlEeaAdjustmentsMtdJson.as[ForeignFhlEeaAdjustments] shouldBe foreignFhlEeaAdjustments
      }
    }
  }
  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(foreignFhlEeaAdjustments) shouldBe foreignFhlEeaAdjustmentsDownstreamJson
      }
    }
  }
}
