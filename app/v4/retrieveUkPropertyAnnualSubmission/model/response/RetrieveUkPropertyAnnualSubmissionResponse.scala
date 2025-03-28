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

package v4.retrieveUkPropertyAnnualSubmission.model.response

import shared.models.domain.Timestamp
import play.api.libs.functional.syntax._
import play.api.libs.json._
import v4.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukFhlProperty.Def1_Retrieve_UkFhlProperty
import v4.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukNonFhlProperty.Def1_Retrieve_UkNonFhlProperty

sealed trait RetrieveUkPropertyAnnualSubmissionResponse

object RetrieveUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[RetrieveUkPropertyAnnualSubmissionResponse] = { case def1: Def1_RetrieveUkPropertyAnnualSubmissionResponse =>
    Json.toJsObject(def1)
  }

}

case class Def1_RetrieveUkPropertyAnnualSubmissionResponse(
    submittedOn: Timestamp,
    ukFhlProperty: Option[Def1_Retrieve_UkFhlProperty],
    ukNonFhlProperty: Option[Def1_Retrieve_UkNonFhlProperty]
) extends RetrieveUkPropertyAnnualSubmissionResponse

object Def1_RetrieveUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[Def1_RetrieveUkPropertyAnnualSubmissionResponse] = Json.writes[Def1_RetrieveUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def1_RetrieveUkPropertyAnnualSubmissionResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "ukFhlProperty").readNullable[Def1_Retrieve_UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[Def1_Retrieve_UkNonFhlProperty]
  )(Def1_RetrieveUkPropertyAnnualSubmissionResponse.apply _)

}
