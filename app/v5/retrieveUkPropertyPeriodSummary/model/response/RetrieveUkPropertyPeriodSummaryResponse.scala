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

package v5.retrieveUkPropertyPeriodSummary.model.response

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Json, OWrites, Reads, __}
import shared.models.domain.Timestamp
import v5.retrieveUkPropertyPeriodSummary.def1.model.response.{Def1_Retrieve_UkFhlProperty, Def1_Retrieve_UkNonFhlProperty}
import v5.retrieveUkPropertyPeriodSummary.def2.model.response.{Def2_Retrieve_UkFhlProperty, Def2_Retrieve_UkNonFhlProperty}

sealed trait RetrieveUkPropertyPeriodSummaryResponse

object RetrieveUkPropertyPeriodSummaryResponse {

  implicit val writes: OWrites[RetrieveUkPropertyPeriodSummaryResponse] = {
    case def1: Def1_RetrieveUkPropertyPeriodSummaryResponse => Json.toJsObject(def1)
    case def2: Def2_RetrieveUkPropertyPeriodSummaryResponse => Json.toJsObject(def2)
  }

}

case class Def1_RetrieveUkPropertyPeriodSummaryResponse(
    submittedOn: Timestamp,
    fromDate: String,
    toDate: String,
    // periodCreationDate: Option[String], // To be reinstated, see MTDSA-15575
    ukFhlProperty: Option[Def1_Retrieve_UkFhlProperty],
    ukNonFhlProperty: Option[Def1_Retrieve_UkNonFhlProperty]
) extends RetrieveUkPropertyPeriodSummaryResponse

object Def1_RetrieveUkPropertyPeriodSummaryResponse {

  implicit val writes: OWrites[Def1_RetrieveUkPropertyPeriodSummaryResponse] = Json.writes[Def1_RetrieveUkPropertyPeriodSummaryResponse]

  implicit val reads: Reads[Def1_RetrieveUkPropertyPeriodSummaryResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
//      (__ \ "periodCreationDate").readNullable[String] and // To be reinstated, see MTDSA-15575
      (__ \ "ukFhlProperty").readNullable[Def1_Retrieve_UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[Def1_Retrieve_UkNonFhlProperty]
  )(Def1_RetrieveUkPropertyPeriodSummaryResponse.apply)

}

case class Def2_RetrieveUkPropertyPeriodSummaryResponse(submittedOn: Timestamp,
                                                        fromDate: String,
                                                        toDate: String,
                                                        // periodCreationDate: Option[String], // To be reinstated, see MTDSA-15575
                                                        ukFhlProperty: Option[Def2_Retrieve_UkFhlProperty],
                                                        ukNonFhlProperty: Option[Def2_Retrieve_UkNonFhlProperty])
    extends RetrieveUkPropertyPeriodSummaryResponse

object Def2_RetrieveUkPropertyPeriodSummaryResponse {

  implicit val writes: OWrites[Def2_RetrieveUkPropertyPeriodSummaryResponse] = Json.writes[Def2_RetrieveUkPropertyPeriodSummaryResponse]

  implicit val reads: Reads[Def2_RetrieveUkPropertyPeriodSummaryResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
      //      (__ \ "periodCreationDate").readNullable[String] and // To be reinstated, see MTDSA-15575
      (__ \ "ukFhlProperty").readNullable[Def2_Retrieve_UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[Def2_Retrieve_UkNonFhlProperty]
  )(Def2_RetrieveUkPropertyPeriodSummaryResponse.apply)

}
