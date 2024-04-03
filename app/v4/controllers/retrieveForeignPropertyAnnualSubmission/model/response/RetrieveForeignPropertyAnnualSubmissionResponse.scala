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

package v4.controllers.retrieveForeignPropertyAnnualSubmission.model.response

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import api.models.domain.Timestamp
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v4.controllers.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignFhlEea.Def1_Retrieve_ForeignFhlEeaEntry
import v4.controllers.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignProperty.Def1_Retrieve_ForeignPropertyEntry
import v4.controllers.retrieveForeignPropertyAnnualSubmission.model.response.Def1_RetrieveForeignPropertyAnnualSubmissionResponse.Def1_RetrieveForeignAnnualSubmissionLinksFactory
import v4.hateoas.HateoasLinks

sealed trait RetrieveForeignPropertyAnnualSubmissionResponse

object RetrieveForeignPropertyAnnualSubmissionResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveForeignPropertyAnnualSubmissionResponse] = { case def1: Def1_RetrieveForeignPropertyAnnualSubmissionResponse =>
    Json.toJsObject(def1)
  }

  implicit object RetrieveAnnualSubmissionLinksFactory
      extends HateoasLinksFactory[RetrieveForeignPropertyAnnualSubmissionResponse, RetrieveForeignPropertyAnnualSubmissionHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveForeignPropertyAnnualSubmissionHateoasData): Seq[Link] =
      Def1_RetrieveForeignAnnualSubmissionLinksFactory.links(appConfig, data)

  }

}

case class Def1_RetrieveForeignPropertyAnnualSubmissionResponse(submittedOn: Timestamp,
                                                                foreignFhlEea: Option[Def1_Retrieve_ForeignFhlEeaEntry],
                                                                foreignNonFhlProperty: Option[Seq[Def1_Retrieve_ForeignPropertyEntry]])
    extends RetrieveForeignPropertyAnnualSubmissionResponse

object Def1_RetrieveForeignPropertyAnnualSubmissionResponse extends HateoasLinks {

  implicit val writes: OWrites[Def1_RetrieveForeignPropertyAnnualSubmissionResponse] =
    Json.writes[Def1_RetrieveForeignPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def1_RetrieveForeignPropertyAnnualSubmissionResponse] = (
    (JsPath \ "submittedOn").read[Timestamp] and
      (JsPath \ "foreignFhlEea").readNullable[Def1_Retrieve_ForeignFhlEeaEntry] and
      (JsPath \ "foreignProperty").readNullable[Seq[Def1_Retrieve_ForeignPropertyEntry]]
  )(Def1_RetrieveForeignPropertyAnnualSubmissionResponse.apply _)

  implicit object Def1_RetrieveForeignAnnualSubmissionLinksFactory
      extends HateoasLinksFactory[Def1_RetrieveForeignPropertyAnnualSubmissionResponse, RetrieveForeignPropertyAnnualSubmissionHateoasData] {

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
