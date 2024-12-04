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
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import utils.JsonWritesUtil.writesFrom
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.ukFhlProperty.RetrieveUkFhlProperty
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty.{RetrieveUkProperty => Def1_RetrieveUkProperty}
import v5.retrieveUkPropertyAnnualSubmission.def2.model.response.{RetrieveUkProperty => Def2_RetrieveUkProperty}

sealed abstract class RetrieveUkPropertyAnnualSubmissionResponse {
  def hasUkData: Boolean
}

object RetrieveUkPropertyAnnualSubmissionResponse {

  case class Def1_RetrieveUkPropertyAnnualSubmissionResponse(
      submittedOn: Timestamp,
      ukFhlProperty: Option[RetrieveUkFhlProperty],
      ukProperty: Option[Def1_RetrieveUkProperty]
  ) extends RetrieveUkPropertyAnnualSubmissionResponse {
    override def hasUkData: Boolean = ukFhlProperty.isDefined || ukProperty.isDefined
  }

  object Def1_RetrieveUkPropertyAnnualSubmissionResponse {

    implicit val reads: Reads[Def1_RetrieveUkPropertyAnnualSubmissionResponse] = (
      (__ \ "submittedOn").read[Timestamp] ~
        (__ \ "ukFhlProperty").readNullable[RetrieveUkFhlProperty] ~
        (__ \ "ukOtherProperty").readNullable[Def1_RetrieveUkProperty]
    )(Def1_RetrieveUkPropertyAnnualSubmissionResponse.apply _)

  }

  // Note: ukProperty is effectively mandatory.
  // It will only not be present in a successful response from downstream if a businessId
  // corresponds to a non-UK property is used and in this case we send back an error.
  case class Def2_RetrieveUkPropertyAnnualSubmissionResponse(submittedOn: Timestamp, ukProperty: Option[Def2_RetrieveUkProperty])
      extends RetrieveUkPropertyAnnualSubmissionResponse {
    override def hasUkData: Boolean = ukProperty.isDefined
  }

  object Def2_RetrieveUkPropertyAnnualSubmissionResponse {

    implicit val reads: Reads[Def2_RetrieveUkPropertyAnnualSubmissionResponse] = (
      (__ \ "submittedOn").read[Timestamp] ~
        (__ \ "ukOtherProperty").readNullable[Def2_RetrieveUkProperty]
    )(Def2_RetrieveUkPropertyAnnualSubmissionResponse.apply _)

  }

  implicit def writes(implicit
      w1: OWrites[Def1_RetrieveUkProperty],
      w2: OWrites[Def2_RetrieveUkProperty]
  ): OWrites[RetrieveUkPropertyAnnualSubmissionResponse] =
    writesFrom {
      case def1: Def1_RetrieveUkPropertyAnnualSubmissionResponse => Json.writes[Def1_RetrieveUkPropertyAnnualSubmissionResponse].writes(def1)
      case def2: Def2_RetrieveUkPropertyAnnualSubmissionResponse => Json.writes[Def2_RetrieveUkPropertyAnnualSubmissionResponse].writes(def2)
    }

}
