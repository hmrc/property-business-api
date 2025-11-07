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

package v6.retrieveForeignPropertyDetails.def1.model.response

//import play.api.libs.json.{JsString, JsValue, Json}
import shared.utils.UnitSpec
import shared.utils.enums.EnumJsonSpecSupport
//import v6.retrieveForeignPropertyDetails.def1.model.Def1_RetrieveForeignPropertyDetailsFixture
import v6.retrieveForeignPropertyDetails.def1.model.response.EndReason.*

class EndReasonSpec extends UnitSpec with EnumJsonSpecSupport {

//  "EndReason" when {
//
//    // val downstreamJson: JsValue = JsString("noLongerRentingPropertyOut")
//    // val json: JsValue    = JsString("noLongerRentingPropertyOut")
//    val mtdJson: JsValue = ((fullMtdJson \ "foreignPropertyDetails")(0) \ "endReason").get
//
//    val endReason: EndReason = EndReason.`no-longer-renting-property-out`
//
//    "read from valid JSON" should {
////      "return the parsed object" in {
////        // val result: EndReason = json.as[EndReason]
////        // result shouldBe endReason
////        json.as[EndReason] shouldBe endReason
////      }
//      "deserialize from valid downstream JSON" in {
//        // Given
//        val json = JsString("noLongerRentingPropertyOut")
//
//        // When
//        val result = json.as[EndReason]
//
//        // Then
//        result shouldBe EndReason.`no-longer-renting-property-out`
//      }
//    }
//
//    "write to valid JSON" should {
//      "return the expected JSON" in {
//        Json.toJson(endReason) shouldBe mtdJson
//      }
//    }
//  }

  testRoundTrip[EndReason](
    ("added-in-error", `added-in-error`),
    ("disposal", `disposal`),
    ("no-longer-renting-property-out", `no-longer-renting-property-out`),
    ("cessation", `cessation`)
  )

  "EndReason" when {
    "getting downstream EndReason" must {
      "work" in {
        `disposal` shouldBe EndReason.`disposal`
        // `carry-sideways`.toReliefClaimed shouldBe ReliefClaimed.`CSGI`
        // `carry-forward-to-carry-sideways`.toReliefClaimed shouldBe ReliefClaimed.`CFCSGI`
        // `carry-sideways-fhl`.toReliefClaimed shouldBe ReliefClaimed.`CSFHL`
      }
    }
  }

}
