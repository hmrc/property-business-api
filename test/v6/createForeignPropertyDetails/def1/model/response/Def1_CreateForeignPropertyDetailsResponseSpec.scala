/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.createForeignPropertyDetails.def1.model.response

import play.api.libs.json.*
import shared.utils.UnitSpec

class Def1_CreateForeignPropertyDetailsResponseSpec extends UnitSpec {

  private val response = Def1_CreateForeignPropertyDetailsResponse(
    propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb"
  )

  val responseJson: JsValue = Json.parse("""{
      |  "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb"
      |}""".stripMargin)

  val mtdJson: JsObject = JsObject.empty

  "reads" should {
    "return a valid model" in {
      responseJson.as[Def1_CreateForeignPropertyDetailsResponse] shouldBe response
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(response) shouldBe responseJson
      }
    }
  }

}
