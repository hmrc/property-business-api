/*
 * Copyright 2021 HM Revenue & Customs
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

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json._
import v2.hateoas.{HateoasLinks, HateoasLinksFactory}
import v2.models.hateoas.{HateoasData, Link}
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukFhlProperty.UkFhlProperty
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty.UkNonFhlProperty

case class RetrieveUkPropertyAnnualSubmissionResponse(submittedOn: String,
                                                      ukFhlProperty: Option[UkFhlProperty],
                                                      ukNonFhlProperty: Option[UkNonFhlProperty])

object RetrieveUkPropertyAnnualSubmissionResponse extends HateoasLinks {
  implicit val writes: OWrites[RetrieveUkPropertyAnnualSubmissionResponse] = Json.writes[RetrieveUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[RetrieveUkPropertyAnnualSubmissionResponse] = (
    (__ \ "submittedOn").read[String] and
      (__ \ "ukFhlProperty").readNullable[UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[UkNonFhlProperty]
  )(RetrieveUkPropertyAnnualSubmissionResponse.apply _)

  implicit object RetrieveAnnualSubmissionLinksFactory extends
    HateoasLinksFactory[RetrieveUkPropertyAnnualSubmissionResponse, RetrieveUkPropertyAnnualSubmissionHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveUkPropertyAnnualSubmissionHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendUkPropertyAnnualSubmission(appConfig, nino, businessId, taxYear),
        retrieveUkPropertyAnnualSubmission(appConfig, nino, businessId, taxYear),
        deletePropertyAnnualSubmission(appConfig, nino, businessId, taxYear)
      )
    }
  }
}

case class RetrieveUkPropertyAnnualSubmissionHateoasData(nino: String, businessId: String, taxYear: String) extends HateoasData