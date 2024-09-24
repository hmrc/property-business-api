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

package v5.retrieveUkPropertyAnnualSubmission.model.response

import api.models.domain.Timestamp
import play.api.libs.functional.syntax._
import play.api.libs.json._
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.ukFhlProperty.RetrieveUkFhlProperty
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty.{RetrieveUkProperty => Def1RetrieveUkProperty}
import v5.retrieveUkPropertyAnnualSubmission.def2.model.response.ukProperty.{RetrieveUkProperty => Def2RetrieveUkProperty}

sealed trait RetrieveUkPropertyAnnualSubmissionResponse {
  def isUkResult: Boolean
}

object RetrieveUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[RetrieveUkPropertyAnnualSubmissionResponse] = {
    case def1: Def1_RetrieveUkPropertyAnnualSubmissionResponse => Json.toJsObject(def1)
    case def2: Def2_RetrieveUkPropertyAnnualSubmissionResponse => Json.toJsObject(def2)
  }

}

case class Def1_RetrieveUkPropertyAnnualSubmissionResponse(
    submittedOn: Timestamp,
    ukFhlProperty: Option[RetrieveUkFhlProperty],
    ukProperty: Option[Def1RetrieveUkProperty]
) extends RetrieveUkPropertyAnnualSubmissionResponse {
  override def isUkResult: Boolean = ukFhlProperty.nonEmpty || ukProperty.nonEmpty
}

object Def1_RetrieveUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[Def1_RetrieveUkPropertyAnnualSubmissionResponse] = Json.writes[Def1_RetrieveUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def1_RetrieveUkPropertyAnnualSubmissionResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "ukFhlProperty").readNullable[RetrieveUkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[Def1RetrieveUkProperty]
  )(Def1_RetrieveUkPropertyAnnualSubmissionResponse.apply _)

}

case class Def2_RetrieveUkPropertyAnnualSubmissionResponse(
    submittedOn: Timestamp,
    ukProperty: Option[Def2RetrieveUkProperty]
) extends RetrieveUkPropertyAnnualSubmissionResponse {
  override def isUkResult: Boolean = ukProperty.nonEmpty
}

object Def2_RetrieveUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[Def2_RetrieveUkPropertyAnnualSubmissionResponse] = Json.writes[Def2_RetrieveUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def2_RetrieveUkPropertyAnnualSubmissionResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "ukOtherProperty").readNullable[Def2RetrieveUkProperty]
  )(Def2_RetrieveUkPropertyAnnualSubmissionResponse.apply _)

}
