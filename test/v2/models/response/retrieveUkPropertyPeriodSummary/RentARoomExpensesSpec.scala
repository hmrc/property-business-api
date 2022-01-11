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

package v2.models.response.retrieveUkPropertyPeriodSummary

import fixtures.RetrieveUkPropertyPeriodSummary.ResponseModelsFixture
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class RentARoomExpensesSpec extends UnitSpec with ResponseModelsFixture {
  "RentARoomExpenses" when {
    val downstreamJson: JsValue = (fullDownstreamJson \ "ukOtherProperty" \ "expenses" \ "ukOtherRentARoom").get
    val mtdJson: JsValue = (fullMtdJson \ "ukNonFhlProperty" \ "expenses" \ "rentARoom").get
    val model: RentARoomExpenses = ukNonFhlRentARoomExpensesModel
    "read from valid JSON" should {
      "return the expected model" in {
        downstreamJson.as[RentARoomExpenses] shouldBe model
      }
    }

    "written JSON" should {
      "return the expected JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }
}
