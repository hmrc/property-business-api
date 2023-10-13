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

package v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignNonFhl

import play.api.libs.json.Json
import api.support.UnitSpec
import v2.models.request.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionFixture

class ForeignNonFhlAllowancesSpec extends UnitSpec with CreateAmendForeignPropertyAnnualSubmissionFixture {

  "reads" when {
    "passed valid mtd JSON" should {
      "return the model" in {
        foreignNonFhlAllowancesMtdJson.as[ForeignNonFhlAllowances] shouldBe foreignNonFhlAllowances
      }
    }
  }

  "writes" when {
    "passed a model" should {
      "return downstream JSON" in {
        Json.toJson(foreignNonFhlAllowances) shouldBe foreignNonFhlAllowancesDownstreamJson
      }
    }
  }

}
