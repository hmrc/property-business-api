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

package v2.models.response.retrieveUkPropertyPeriodSummary

import fixtures.RetrieveUkPropertyPeriodSummary.ResponseModelsFixture
import play.api.libs.json.{JsValue, Json}
import api.support.UnitSpec

class NonFhlPropertyIncomeSpec extends UnitSpec with ResponseModelsFixture {

  "NonFhlPropertyIncome" when {
    val downstreamJson: JsValue     = (fullDownstreamJson \ "ukOtherProperty" \ "income").get
    val mtdJson: JsValue            = (fullMtdJson \ "ukNonFhlProperty" \ "income").get
    val model: NonFhlPropertyIncome = ukNonFhlIncomeModel
    "read from valid JSON" should {
      "return the expected model" in {
        downstreamJson.as[NonFhlPropertyIncome] shouldBe model
      }
    }

    "written JSON" should {
      "return the expected JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

}
