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

package v6.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignProperty

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v6.createAmendForeignPropertyAnnualSubmission.def1.model.request.Def1_Fixtures

class Def1_Create_Amend_ForeignAdjustmentsSpec extends UnitSpec with Def1_Fixtures {

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        foreignAdjustmentsMtdJson.as[Def1_Create_Amend_ForeignAdjustments] shouldBe foreignAdjustments
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(foreignAdjustments) shouldBe foreignAdjustmentsDownstreamJson
      }
    }
  }

}
