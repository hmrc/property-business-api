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

package v6.retrieveUkPropertyAnnualSubmission.def3.model.response

import api.models.domain.Timestamp
import play.api.libs.functional.syntax.*
import play.api.libs.json.{Json, OWrites, Reads, __}
import v6.retrieveUkPropertyAnnualSubmission.model.response.RetrieveUkPropertyAnnualSubmissionResponse

// Note: ukProperty is effectively mandatory but this is not reflected in the downstream spec
case class Def3_RetrieveUkPropertyAnnualSubmissionResponse(submittedOn: Timestamp, ukProperty: Option[RetrieveUkProperty])
    extends RetrieveUkPropertyAnnualSubmissionResponse {
  override def hasUkData: Boolean = ukProperty.isDefined
}

object Def3_RetrieveUkPropertyAnnualSubmissionResponse {
  implicit val writes: OWrites[Def3_RetrieveUkPropertyAnnualSubmissionResponse] = Json.writes[Def3_RetrieveUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def3_RetrieveUkPropertyAnnualSubmissionResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "ukOtherProperty").readNullable[RetrieveUkProperty]
  )(Def3_RetrieveUkPropertyAnnualSubmissionResponse.apply)

}
