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

package v5.retrieveUkPropertyCumulativeSummary.def1.model.response

import shared.models.domain.Timestamp
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, OWrites, Reads, __}
import v5.retrieveUkPropertyCumulativeSummary.model.response.RetrieveUkPropertyCumulativeSummaryResponse

// Note: ukProperty is effectively mandatory.
// It will only not be present in a successful response from downstream if a businessId
// corresponds to a non-UK property is used and in this case we send back an error.
case class Def1_RetrieveUkPropertyCumulativeSummaryResponse(
    submittedOn: Timestamp,
    fromDate: String,
    toDate: String,
    ukProperty: Option[UkProperty]
) extends RetrieveUkPropertyCumulativeSummaryResponse {
  override def hasUkData: Boolean = ukProperty.isDefined
}

object Def1_RetrieveUkPropertyCumulativeSummaryResponse {

  implicit val writes: OWrites[Def1_RetrieveUkPropertyCumulativeSummaryResponse] = Json.writes[Def1_RetrieveUkPropertyCumulativeSummaryResponse]

  implicit val reads: Reads[Def1_RetrieveUkPropertyCumulativeSummaryResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
      (__ \ "ukOtherProperty").readNullable[UkProperty]
  )(Def1_RetrieveUkPropertyCumulativeSummaryResponse.apply _)

}
