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

package v4.retrieveUkPropertyPeriodSummary.def2.model.response

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v4.retrieveUkPropertyPeriodSummary.def2.model.Def2_RetrieveUkPropertyPeriodSummaryFixture

class Def2_Retrieve_FhlPropertyExpensesSpec extends UnitSpec with Def2_RetrieveUkPropertyPeriodSummaryFixture {

  "FhlPropertyExpenses" when {
    val downstreamJson: JsValue = (fullDownstreamJson \ "ukFhlProperty" \ "expenses").get
    val mtdJson: JsValue        = (fullMtdJson \ "ukFhlProperty" \ "expenses").get

    "read from valid JSON" should {
      "return the parsed object" in {
        val result = downstreamJson.as[Def2_Retrieve_FhlPropertyExpenses]
        result shouldBe fhlPropertyExpenses
      }
    }

    "written JSON" should {
      "return the expected JSON" in {
        val result = Json.toJson(fhlPropertyExpenses)
        result shouldBe mtdJson
      }
    }
  }

}
