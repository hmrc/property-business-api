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

package v6.updateForeignPropertyDetails.def1.model

import play.api.libs.json.{JsValue, Json}
import v6.updateForeignPropertyDetails.def1.model.request.Def1_UpdateForeignPropertyDetailsRequestBody

object Def1_UpdateForeignPropertyDetailsFixtures {

  val def1_UpdateForeignPropertyDetailsModel: Def1_UpdateForeignPropertyDetailsRequestBody = Def1_UpdateForeignPropertyDetailsRequestBody(
    "Bob & Bobby Co",
    Some("2026-08-24"),
    Some("no-longer-renting-property-out")
  )

  val def1_UpdateForeignPropertyDetailsMinimumModel: Def1_UpdateForeignPropertyDetailsRequestBody = Def1_UpdateForeignPropertyDetailsRequestBody(
    "Bob & Bobby Co",
    None,
    None
  )

  val def1_UpdateForeignPropertyDetailsDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "foreignPropertyDetails": {
      |    "propertyName": "Bob & Bobby Co",
      |    "endDate": "2026-08-24",
      |    "endReason": "noLongerRentingPropertyOut"
      |  }
      |}
    """.stripMargin
  )

  val def1_UpdateForeignPropertyDetailsMtdJson: JsValue = Json.parse(
    """
      |{
      |  "propertyName": "Bob & Bobby Co",
      |  "endDate": "2026-08-24",
      |  "endReason": "no-longer-renting-property-out"
      |}
    """.stripMargin
  )

  val def1_UpdateForeignPropertyDetailsMinimumMtdJson: JsValue = Json.parse(
    """
      |{
      |  "propertyName": "Bob & Bobby Co"
      |}
    """.stripMargin
  )

}
