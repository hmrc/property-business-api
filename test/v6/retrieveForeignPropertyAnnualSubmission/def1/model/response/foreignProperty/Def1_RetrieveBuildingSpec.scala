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
import v6.retrieveForeignPropertyAnnualSubmission.def1.model.response.foreignProperty.RetrieveBuilding

class Def1_RetrieveBuildingSpec extends UnitSpec {

  val requestBody: RetrieveBuilding =
    RetrieveBuilding(
      Some("house name"),
      Some("house number"),
      "GF49JH"
    )

  val validMtdJson: JsValue = Json.parse("""
      |{
      |  "name": "house name",
      |  "number": "house number",
      |  "postcode": "GF49JH"
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse("""
      |{
      |  "name": "house name",
      |  "number": "house number",
      |  "postCode": "GF49JH"
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validDownstreamJson.as[RetrieveBuilding] shouldBe requestBody
        validDownstreamJson.as[RetrieveBuilding] shouldBe requestBody
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe validMtdJson
      }
    }
  }

}
