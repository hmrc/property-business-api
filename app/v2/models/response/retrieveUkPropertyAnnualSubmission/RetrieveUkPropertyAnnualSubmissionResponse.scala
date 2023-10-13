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

package v2.models.response.retrieveUkPropertyAnnualSubmission

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import api.models.domain.Timestamp
import api.config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json._
import v2.hateoas.HateoasLinks
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukFhlProperty.UkFhlProperty
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty.UkNonFhlProperty

case class RetrieveUkPropertyAnnualSubmissionResponse(submittedOn: Timestamp,
                                                      ukFhlProperty: Option[UkFhlProperty],
                                                      ukNonFhlProperty: Option[UkNonFhlProperty])

object RetrieveUkPropertyAnnualSubmissionResponse extends HateoasLinks {
  implicit val writes: OWrites[RetrieveUkPropertyAnnualSubmissionResponse] = Json.writes[RetrieveUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[RetrieveUkPropertyAnnualSubmissionResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "ukFhlProperty").readNullable[UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[UkNonFhlProperty]
  )(RetrieveUkPropertyAnnualSubmissionResponse.apply _)

  implicit object RetrieveAnnualSubmissionLinksFactory
      extends HateoasLinksFactory[RetrieveUkPropertyAnnualSubmissionResponse, RetrieveUkPropertyAnnualSubmissionHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveUkPropertyAnnualSubmissionHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendUkPropertyAnnualSubmission(appConfig, nino, businessId, taxYear),
        retrieveUkPropertyAnnualSubmission(appConfig, nino, businessId, taxYear, self = true),
        deletePropertyAnnualSubmission(appConfig, nino, businessId, taxYear)
      )
    }

  }

}

case class RetrieveUkPropertyAnnualSubmissionHateoasData(nino: String, businessId: String, taxYear: String) extends HateoasData
