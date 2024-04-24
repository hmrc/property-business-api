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

package v4.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignNonFhl

import play.api.libs.json.Json
import support.UnitSpec
import v4.fixtures.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionFixture

class Def1_Create_Amend_ForeignNonFhlAdjustmentsSpec extends UnitSpec with CreateAmendForeignPropertyAnnualSubmissionFixture {

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        foreignNonFhlAdjustmentsMtdJson.as[Def1_Create_Amend_ForeignNonFhlAdjustments] shouldBe foreignNonFhlAdjustments
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(foreignNonFhlAdjustments) shouldBe foreignNonFhlAdjustmentsDownstreamJson
      }
    }
  }

}