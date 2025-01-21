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

package v6.retrieveForeignPropertyAnnualSubmission.def1.model.response.foreignProperty

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec
import v6.retrieveForeignPropertyAnnualSubmission.def1.model.response.foreignProperty.RetrieveFirstYear

class Def1_RetrieveFirstYearSpec extends UnitSpec {

  val responseBody: RetrieveFirstYear =
    RetrieveFirstYear(
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
        validJson.as[RetrieveFirstYear] shouldBe responseBody
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
