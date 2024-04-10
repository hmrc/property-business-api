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

package v4.controllers.retrieveUkPropertyPeriodSummary.def2.model.response

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v4.controllers.retrieveUkPropertyPeriodSummary.def2.model.Def2_RetrieveUkPropertyPeriodSummaryFixture

class Def2_Retrieve_UkFhlPropertySpec extends UnitSpec with Def2_RetrieveUkPropertyPeriodSummaryFixture {

  "UkFhlProperty" when {
    val downstreamJson: JsValue = (fullDownstreamJson \ "ukFhlProperty").get
    val mtdJson: JsValue        = (fullMtdJson \ "ukFhlProperty").get
    val model: Def2_Retrieve_UkFhlProperty    = ukFhlPropertyModel
    "read from valid JSON" should {
      "return the expected model" in {
        downstreamJson.as[Def2_Retrieve_UkFhlProperty] shouldBe model
      }
    }

    "written JSON" should {
      "return the expected JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

}
