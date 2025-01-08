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

package v5.retrieveForeignPropertyCumulativeSummary.def1.model.response

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec
import v5.retrieveForeignPropertyCumulativeSummary.def1.model.Def1_RetrieveForeignPropertyCumulativeSummaryFixture

class ForeignPropertyEntrySpec extends UnitSpec with Def1_RetrieveForeignPropertyCumulativeSummaryFixture {

  "ForeignPropertyEntry" when {
    val downstreamJson: JsValue = (fullDownstreamJson \ "foreignProperty")(0)
    val mtdJson: JsValue        = (fullMtdJson \ "foreignProperty")(0)

    "read from valid JSON" should {
      "return the parsed object" in {
        downstreamJson.as[ForeignPropertyEntry] shouldBe foreignPropertyEntry
      }
    }

    "written JSON" should {
      "return the expected JSON" in {
        Json.toJson(foreignPropertyEntry) shouldBe mtdJson
      }
    }
  }

}
