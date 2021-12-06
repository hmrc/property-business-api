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

package v2.models.response.retrieveForeignPropertyAnnualSubmission

import config.AppConfig
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v2.hateoas.{HateoasLinks, HateoasLinksFactory}
import play.api.libs.functional.syntax._
import v2.models.hateoas.{HateoasData, Link}
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea.ForeignFhlEeaEntry
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty.ForeignPropertyEntry

case class RetrieveForeignPropertyAnnualSubmissionResponse(
                                                            submittedOn: String,
                                                            foreignFhlEea: Option[ForeignFhlEeaEntry],
                                                            foreignNonFhlProperty: Option[Seq[ForeignPropertyEntry]])

object RetrieveForeignPropertyAnnualSubmissionResponse extends HateoasLinks {
  implicit  val writes: Writes[RetrieveForeignPropertyAnnualSubmissionResponse] = Json.writes[RetrieveForeignPropertyAnnualSubmissionResponse]
  implicit  val reads: Reads[RetrieveForeignPropertyAnnualSubmissionResponse] = (
    (JsPath \ "submittedOn").read[String] and
      (JsPath \ "foreignFhlEea").readNullable[ForeignFhlEeaEntry] and
      (JsPath \ "foreignProperty").readNullable[Seq[ForeignPropertyEntry]]
  )(RetrieveForeignPropertyAnnualSubmissionResponse.apply _)

  implicit object RetrieveAnnualSubmissionLinksFactory extends
    HateoasLinksFactory[RetrieveForeignPropertyAnnualSubmissionResponse, RetrieveForeignPropertyAnnualSubmissionHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveForeignPropertyAnnualSubmissionHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendForeignPropertyAnnualSubmission(appConfig, nino, businessId, taxYear),
        retrieveForeignPropertyAnnualSubmission(appConfig, nino, businessId, taxYear, self = true),
        deletePropertyAnnualSubmission(appConfig, nino, businessId, taxYear)
      )
    }
  }
}

case class RetrieveForeignPropertyAnnualSubmissionHateoasData(nino: String, businessId: String, taxYear: String) extends HateoasData