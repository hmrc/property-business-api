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

package v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec

class StructuredBuildingAllowanceSpec extends UnitSpec {

  val responseBody: StructuredBuildingAllowance =
    StructuredBuildingAllowance(
      3000.30,
      Some(
        FirstYear(
          "2020-01-01",
          3000.40
        )),
      Building(
        Some("house name"),
        Some("house number"),
        "GF49JH"
      )
    )

  val validJson: JsValue = Json.parse("""
      |{
      |  "amount": 3000.30,
      |  "firstYear": {
      |    "qualifyingDate": "2020-01-01",
      |    "qualifyingAmountExpenditure": 3000.40
      |  },
      |  "building": {
      |    "name": "house name",
      |    "number": "house number",
      |    "postcode": "GF49JH"
      |  }
      |}
      |""".stripMargin)

  val validIfsJson: JsValue = Json.parse("""
      |{
      |  "amount": 3000.30,
      |  "firstYear": {
      |    "qualifyingDate": "2020-01-01",
      |    "qualifyingAmountExpenditure": 3000.40
      |  },
      |  "building": {
      |    "name": "house name",
      |    "number": "house number",
      |    "postCode": "GF49JH"
      |  }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validIfsJson.as[StructuredBuildingAllowance] shouldBe responseBody
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
