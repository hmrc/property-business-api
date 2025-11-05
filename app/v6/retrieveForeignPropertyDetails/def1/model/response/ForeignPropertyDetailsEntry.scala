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

import common.models.domain.PropertyId
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, OWrites, Reads, __}
import shared.models.domain.Timestamp

case class ForeignPropertyDetailsEntry(submittedOn: Timestamp,
                                       propertyId: PropertyId,
                                       propertyName: String,
                                       countryCode: String,
                                       endDate: Option[String],
                                       endReason: Option[String])

object ForeignPropertyDetailsEntry {

  implicit val writes: OWrites[Def1_RetrieveForeignPropertyDetailsResponse] =
    Json.writes[Def1_RetrieveForeignPropertyDetailsResponse]

  implicit val reads: Reads[Def1_RetrieveForeignPropertyDetailsResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "propertyId").read[PropertyId] and
      (__ \ "propertyName").read[String] and
      (__ \ "countryCode").read[String] and
      (__ \ "endDate").readNullable[Seq[String]] and
      (__ \ "endReason").readNullable[Seq[EndReason]]
  )(Def1_RetrieveForeignPropertyDetailsResponse.apply)

}
