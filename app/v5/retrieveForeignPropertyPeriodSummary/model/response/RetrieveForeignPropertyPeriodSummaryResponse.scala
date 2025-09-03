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

package v5.retrieveForeignPropertyPeriodSummary.model.response

import shared.models.domain.Timestamp
import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v5.retrieveForeignPropertyPeriodSummary.def1.model.response.foreignFhlEea.ForeignFhlEea
import v5.retrieveForeignPropertyPeriodSummary.def1.model.response.foreignNonFhlProperty.ForeignNonFhlProperty

sealed trait RetrieveForeignPropertyPeriodSummaryResponse

object RetrieveForeignPropertyPeriodSummaryResponse {

  implicit val writes: OWrites[RetrieveForeignPropertyPeriodSummaryResponse] = { case def1: Def1_RetrieveForeignPropertyPeriodSummaryResponse =>
    Json.toJsObject(def1)
  }

}

case class Def1_RetrieveForeignPropertyPeriodSummaryResponse(
    submittedOn: Timestamp,
    fromDate: String,
    toDate: String,
    foreignFhlEea: Option[ForeignFhlEea],
    foreignNonFhlProperty: Option[Seq[ForeignNonFhlProperty]]
) extends RetrieveForeignPropertyPeriodSummaryResponse

object Def1_RetrieveForeignPropertyPeriodSummaryResponse {
  implicit val writes: OWrites[Def1_RetrieveForeignPropertyPeriodSummaryResponse] = Json.writes[Def1_RetrieveForeignPropertyPeriodSummaryResponse]

  implicit val reads: Reads[Def1_RetrieveForeignPropertyPeriodSummaryResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "fromDate").read[String] and
      (JsPath \ "toDate").read[String] and
      (JsPath \ "foreignFhlEea").readNullable[ForeignFhlEea] and
      (JsPath \ "foreignProperty").readNullable[Seq[ForeignNonFhlProperty]]
  )(Def1_RetrieveForeignPropertyPeriodSummaryResponse.apply _)

}
