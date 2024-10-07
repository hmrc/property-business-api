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

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v5.retrieveUkPropertyCumulativeSummary.def1.model.Def1_RetrieveUkPropertyCumulativeSummaryFixture

class ExpensesSpec extends UnitSpec with Def1_RetrieveUkPropertyCumulativeSummaryFixture {

  "Expenses" when {
    // Note that downstream field names differ between the 'consolidated' and the 'all other' expenses branches so
    // we must check separately...
    "consolidated" must {
      val downstreamJson: JsValue = (consolidatedDownstreamJson \ "ukOtherProperty" \ "expenses").get
      val mtdJson: JsValue        = (consolidatedMtdJson \ "ukProperty" \ "expenses").get

      "read from valid JSON" should {
        "return the parsed object" in {
          downstreamJson.as[Expenses] shouldBe expensesConsolidated
        }
      }

      "written JSON" should {
        "return the expected JSON" in {
          Json.toJson(expensesConsolidated) shouldBe mtdJson
        }
      }
    }

    "not consolidated" must {
      val downstreamJson: JsValue = (fullDownstreamJson \ "ukOtherProperty" \ "expenses").get
      val mtdJson: JsValue        = (fullMtdJson \ "ukProperty" \ "expenses").get

      "read from valid JSON" should {
        "return the parsed object" in {
          downstreamJson.as[Expenses] shouldBe expenses
        }
      }

      "written JSON" should {
        "return the expected JSON" in {
          Json.toJson(expenses) shouldBe mtdJson
        }
      }
    }

  }

}
