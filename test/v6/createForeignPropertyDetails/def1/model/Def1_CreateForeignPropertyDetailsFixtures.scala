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

package v6.createForeignPropertyDetails.def1.model

import play.api.libs.json.{JsValue, Json}
import v6.createForeignPropertyDetails.def1.model.request.Def1_CreateForeignPropertyDetailsRequestBody
import v6.createForeignPropertyDetails.def1.model.response.Def1_CreateForeignPropertyDetailsResponse

trait Def1_CreateForeignPropertyDetailsFixtures {

  val def1_CreateForeignPropertyDetailsModel: Def1_CreateForeignPropertyDetailsRequestBody = Def1_CreateForeignPropertyDetailsRequestBody(
    "Bob & Bobby Co",
    "FRA",
    Some("2026-08-24"),
    Some("no-longer-renting-property-out")
  )

  val def1_CreateForeignPropertyDetailsWriteJson: JsValue = Json.parse(
    """{
      |"propertyName": "Bob & Bobby Co",
      |"countryCode": "FRA",
      |"endDate": "2026-08-24",
      |"endReason": "noLongerRentingPropertyOut"
      |}""".stripMargin
  )

  val def1_CreateForeignPropertyDetailsReadJson: JsValue = Json.parse(
    """{
      |"propertyName": "Bob & Bobby Co",
      |"countryCode": "FRA",
      |"endDate": "2026-08-24",
      |"endReason": "no-longer-renting-property-out"
      |}""".stripMargin
  )

  val def1_CreateForeignPropertyDetailsResponseModel: Def1_CreateForeignPropertyDetailsResponse = Def1_CreateForeignPropertyDetailsResponse(
    "8e8b8450-dc1b-4360-8109-7067337b42cb"
  )

  val def1_CreateForeignPropertyDetailsResponseJson: JsValue = Json.parse(
    """{
      |"propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb"
      |}""".stripMargin
  )

}
