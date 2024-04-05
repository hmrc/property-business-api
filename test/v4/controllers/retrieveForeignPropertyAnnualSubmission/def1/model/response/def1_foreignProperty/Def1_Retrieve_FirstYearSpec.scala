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

package v4.controllers.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class Def1_Retrieve_FirstYearSpec extends UnitSpec {

  val responseBody: Def1_Retrieve_FirstYear =
    Def1_Retrieve_FirstYear(
      "2020-01-01",
      3000.40
    )

  val validJson: JsValue = Json.parse("""
      |{
      |  "qualifyingDate": "2020-01-01",
      |  "qualifyingAmountExpenditure": 3000.40
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validJson.as[Def1_Retrieve_FirstYear] shouldBe responseBody
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(responseBody) shouldBe validJson
      }
    }
  }

}
