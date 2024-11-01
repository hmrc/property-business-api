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

package v5.retrieveForeignPropertyAnnualSubmission.def2.model.response

import api.models.domain.Timestamp
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v5.retrieveForeignPropertyAnnualSubmission.model.response.RetrieveForeignPropertyAnnualSubmissionResponse

// Note: foreignProperty is effectively mandatory.
// It will only not be present in a successful response from downstream if a businessId
// corresponds to a UK property is used and in this case we send back an error.
case class Def2_RetrieveForeignPropertyAnnualSubmissionResponse(
    submittedOn: Timestamp,
    foreignProperty: Option[Seq[RetrieveForeignPropertyEntry]]
) extends RetrieveForeignPropertyAnnualSubmissionResponse {

  override def hasForeignData: Boolean = foreignProperty.isDefined
}

object Def2_RetrieveForeignPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[Def2_RetrieveForeignPropertyAnnualSubmissionResponse] =
    Json.writes[Def2_RetrieveForeignPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def2_RetrieveForeignPropertyAnnualSubmissionResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "foreignProperty").readNullable[Seq[RetrieveForeignPropertyEntry]]
  )(Def2_RetrieveForeignPropertyAnnualSubmissionResponse.apply _)

}
