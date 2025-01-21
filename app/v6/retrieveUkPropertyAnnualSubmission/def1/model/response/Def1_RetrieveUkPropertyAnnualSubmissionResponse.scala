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

package v6.retrieveUkPropertyAnnualSubmission.def1.model.response

import shared.models.domain.Timestamp
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OWrites, Reads, __}
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukFhlProperty.RetrieveUkFhlProperty
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty.RetrieveUkProperty
import v6.retrieveUkPropertyAnnualSubmission.model.response.RetrieveUkPropertyAnnualSubmissionResponse

case class Def1_RetrieveUkPropertyAnnualSubmissionResponse(
    submittedOn: Timestamp,
    ukFhlProperty: Option[RetrieveUkFhlProperty],
    ukProperty: Option[RetrieveUkProperty]
) extends RetrieveUkPropertyAnnualSubmissionResponse {
  override def hasUkData: Boolean = ukFhlProperty.isDefined || ukProperty.isDefined
}

object Def1_RetrieveUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[Def1_RetrieveUkPropertyAnnualSubmissionResponse] =
    Json.writes[Def1_RetrieveUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def1_RetrieveUkPropertyAnnualSubmissionResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "ukFhlProperty").readNullable[RetrieveUkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[RetrieveUkProperty]
  )(Def1_RetrieveUkPropertyAnnualSubmissionResponse.apply _)

}
