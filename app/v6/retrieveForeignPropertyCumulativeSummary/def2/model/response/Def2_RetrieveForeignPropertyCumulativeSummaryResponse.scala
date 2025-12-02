/*
 * Copyright 2024 HM Revenue & Customs
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

package v6.retrieveForeignPropertyCumulativeSummary.def2.model.response

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, OWrites, Reads, __}
import shared.models.domain.Timestamp
import v6.retrieveForeignPropertyCumulativeSummary.model.response.RetrieveForeignPropertyCumulativeSummaryResponse

case class Def2_RetrieveForeignPropertyCumulativeSummaryResponse(
    submittedOn: Timestamp,
    fromDate: String,
    toDate: String,
    foreignProperty: Seq[ForeignPropertyEntry]
) extends RetrieveForeignPropertyCumulativeSummaryResponse {
  override def hasForeignData: Boolean = true
}

object Def2_RetrieveForeignPropertyCumulativeSummaryResponse {

  implicit val writes: OWrites[Def2_RetrieveForeignPropertyCumulativeSummaryResponse] =
    Json.writes[Def2_RetrieveForeignPropertyCumulativeSummaryResponse]

  implicit val reads: Reads[Def2_RetrieveForeignPropertyCumulativeSummaryResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
      (__ \ "foreignProperty").read[Seq[ForeignPropertyEntry]]
  )(Def2_RetrieveForeignPropertyCumulativeSummaryResponse.apply)

}
