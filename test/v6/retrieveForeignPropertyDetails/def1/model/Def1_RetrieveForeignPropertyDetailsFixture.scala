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

package v6.retrieveForeignPropertyDetails.def1.model

import play.api.libs.json.{JsValue, Json}
import shared.models.domain.Timestamp
import v6.retrieveForeignPropertyDetails.def1.model.response.*

trait Def1_RetrieveForeignPropertyDetailsFixture {

  val fullDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "foreignPropertyDetails": [
      |    {
      |       "submittedOn": "2026-07-07T10:59:47.544Z",
      |       "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
      |       "propertyName": "Bob & Bobby Co",
      |       "countryCode": "FRA",
      |       "endDate": "2026-08-24",
      |       "endReason": "noLongerRentingPropertyOut"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val fullMtdJson: JsValue = Json.parse(
    """
      |{
      |  "foreignPropertyDetails": [
      |    {
      |       "submittedOn": "2026-07-07T10:59:47.544Z",
      |       "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
      |       "propertyName": "Bob & Bobby Co",
      |       "countryCode": "FRA",
      |       "endDate": "2026-08-24",
      |       "endReason": "no-longer-renting-property-out"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val foreignPropertyDetailsEntry: ForeignPropertyDetailsEntry = ForeignPropertyDetailsEntry(
    submittedOn = "2026-07-07T10:59:47.544Z",
    propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb",
    propertyName = "Bob & Bobby Co",
    countryCode = "FRA",
    endDate = Some("2026-08-24"),
    endReason = Some("noLongerRentingPropertyOut")
  )

  val fullResponse: Def1_RetrieveForeignPropertyDetailsResponse = Def1_RetrieveForeignPropertyDetailsResponse(
    foreignPropertyDetails = Seq(foreignPropertyDetailsEntry)
  )

}
