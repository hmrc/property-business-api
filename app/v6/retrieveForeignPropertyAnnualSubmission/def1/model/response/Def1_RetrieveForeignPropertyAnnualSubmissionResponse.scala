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

package v6.retrieveForeignPropertyAnnualSubmission.def1.model.response

import shared.models.domain.Timestamp
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v6.retrieveForeignPropertyAnnualSubmission.def1.model.response.foreignFhlEea.RetrieveForeignFhlEeaEntry
import v6.retrieveForeignPropertyAnnualSubmission.def1.model.response.foreignProperty.RetrieveForeignPropertyEntry
import v6.retrieveForeignPropertyAnnualSubmission.model.response.RetrieveForeignPropertyAnnualSubmissionResponse

case class Def1_RetrieveForeignPropertyAnnualSubmissionResponse(
    submittedOn: Timestamp,
    foreignFhlEea: Option[RetrieveForeignFhlEeaEntry],
    foreignProperty: Option[Seq[RetrieveForeignPropertyEntry]]
) extends RetrieveForeignPropertyAnnualSubmissionResponse {

  override def hasForeignData: Boolean = foreignFhlEea.isDefined || foreignProperty.isDefined
}

object Def1_RetrieveForeignPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[Def1_RetrieveForeignPropertyAnnualSubmissionResponse] =
    Json.writes[Def1_RetrieveForeignPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def1_RetrieveForeignPropertyAnnualSubmissionResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "foreignFhlEea").readNullable[RetrieveForeignFhlEeaEntry] and
      (JsPath \ "foreignProperty").readNullable[Seq[RetrieveForeignPropertyEntry]]
  )(Def1_RetrieveForeignPropertyAnnualSubmissionResponse.apply _)

}
