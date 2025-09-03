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

package v4.retrieveForeignPropertyAnnualSubmission.model.response

import shared.models.domain.Timestamp
import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v4.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignFhlEea.Def1_Retrieve_ForeignFhlEeaEntry
import v4.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignProperty.Def1_Retrieve_ForeignPropertyEntry

sealed trait RetrieveForeignPropertyAnnualSubmissionResponse

object RetrieveForeignPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[RetrieveForeignPropertyAnnualSubmissionResponse] = { case def1: Def1_RetrieveForeignPropertyAnnualSubmissionResponse =>
    Json.toJsObject(def1)
  }

}

case class Def1_RetrieveForeignPropertyAnnualSubmissionResponse(
    submittedOn: Timestamp,
    foreignFhlEea: Option[Def1_Retrieve_ForeignFhlEeaEntry],
    foreignNonFhlProperty: Option[Seq[Def1_Retrieve_ForeignPropertyEntry]]
) extends RetrieveForeignPropertyAnnualSubmissionResponse

object Def1_RetrieveForeignPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[Def1_RetrieveForeignPropertyAnnualSubmissionResponse] =
    Json.writes[Def1_RetrieveForeignPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def1_RetrieveForeignPropertyAnnualSubmissionResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "foreignFhlEea").readNullable[Def1_Retrieve_ForeignFhlEeaEntry] and
      (JsPath \ "foreignProperty").readNullable[Seq[Def1_Retrieve_ForeignPropertyEntry]]
  )(Def1_RetrieveForeignPropertyAnnualSubmissionResponse.apply _)

}
