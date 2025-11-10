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

import play.api.libs.functional.syntax._
import shared.models.domain.Timestamp
import play.api.libs.json._

case class ForeignPropertyDetailsEntry(
    submittedOn: Timestamp,
    propertyId: String,
    propertyName: String,
    countryCode: String,
    endDate: Option[String],
    endReason: Option[EndReason]
)

object ForeignPropertyDetailsEntry {

  implicit val writes: OWrites[ForeignPropertyDetailsEntry] = Json.writes[ForeignPropertyDetailsEntry]

  given Reads[ForeignPropertyDetailsEntry] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "propertyId").read[String] and
      (__ \ "propertyName").read[String] and
      (__ \ "countryCode").read[String] and
      (__ \ "endDate").readNullable[String] and
      (__ \ "endReason").readNullable[String].map { maybeStr =>
        maybeStr.flatMap(str => EndReason.values.find(_.fromDownstream == str))
      }
  )(ForeignPropertyDetailsEntry.apply)

}
