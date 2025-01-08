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

package v4.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukNonFhlProperty

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class Def1_Retrieve_UkNonFhlPropertyBuildingSpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse("""
      |{
      |   "name":"Plaza",
      |   "number":"1",
      |   "postCode":"TF3 4EH"
      |}
      |""".stripMargin)

  val model: Def1_Retrieve_UkNonFhlPropertyBuilding = Def1_Retrieve_UkNonFhlPropertyBuilding(
    name = Some("Plaza"),
    number = Some("1"),
    postcode = "TF3 4EH"
  )

  val mtdJson: JsValue = Json.parse("""
      |{
      |   "name":"Plaza",
      |   "number":"1",
      |   "postcode":"TF3 4EH"
      |}
      |""".stripMargin)

  "reads" should {
    "read JSON into a model" in {
      downstreamJson.as[Def1_Retrieve_UkNonFhlPropertyBuilding] shouldBe model
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }

}
