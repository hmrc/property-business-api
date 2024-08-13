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

package v4.retrieveUkPropertyAnnualSubmission.def2.response.def2_ukProperty

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v4.retrieveUkPropertyAnnualSubmission.def2.model.response.def2_ukProperty.Def2_Retrieve_UkPropertyBuilding

class Def2_Retrieve_UkPropertyBuildingSpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse("""
      |{
      |   "name":"Plaza",
      |   "number":"1",
      |   "postCode":"TF3 4EH"
      |}
      |""".stripMargin)

  val model: Def2_Retrieve_UkPropertyBuilding = Def2_Retrieve_UkPropertyBuilding(
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
      downstreamJson.as[Def2_Retrieve_UkPropertyBuilding] shouldBe model
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }

}
