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

package v4.controllers.retrieveUkPropertyPeriodSummary.model.response

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import api.models.domain.Timestamp
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{Json, OWrites, Reads, __}
import v4.controllers.retrieveUkPropertyPeriodSummary.def2.model.response.{Def2_Retrieve_ConsolidatedUkNonFhlProperty, Def2_Retrieve_UkFhlProperty}
import v4.controllers.retrieveUkPropertyPeriodSummary.model.response.Def2_RetrieveUkPropertyPeriodSummaryResponse.Def2_RetrieveUkPropertyPeriodSummaryResponseLinksFactory
import v4.hateoas.HateoasLinks

sealed trait RetrieveUkPropertyPeriodSummaryResponse

object RetrieveUkPropertyPeriodSummaryConsolidatedResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveUkPropertyPeriodSummaryResponse] = {
    case def2: Def1_RetrieveUkPropertyPeriodSummaryResponse => Json.toJsObject(def2)
  }

  implicit object RetrieveUkPropertyPeriodSummaryResponseLinksFactory
      extends HateoasLinksFactory[RetrieveUkPropertyPeriodSummaryResponse, RetrieveUkPropertyPeriodSummaryHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveUkPropertyPeriodSummaryHateoasData): Seq[Link] =
      Def2_RetrieveUkPropertyPeriodSummaryResponseLinksFactory.links(appConfig, data)

  }

}

case class Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse(submittedOn: Timestamp,
                                                        fromDate: String,
                                                        toDate: String,
                                                        // periodCreationDate: Option[String], // To be reinstated, see MTDSA-15575
                                                        ukFhlProperty: Option[Def2_Retrieve_UkFhlProperty],
                                                        ukNonFhlProperty: Option[Def2_Retrieve_ConsolidatedUkNonFhlProperty])
  extends RetrieveUkPropertyPeriodSummaryResponse

object Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse extends HateoasLinks {

  implicit val writes: OWrites[Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse] = Json.writes[Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse]

  implicit val reads: Reads[Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
      //      (__ \ "periodCreationDate").readNullable[String] and // To be reinstated, see MTDSA-15575
      (__ \ "ukFhlProperty").readNullable[Def2_Retrieve_UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[Def2_Retrieve_ConsolidatedUkNonFhlProperty]
    )(Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse.apply _)

  implicit object Def2_RetrieveUkPropertyPeriodSummaryResponseLinksFactory
    extends HateoasLinksFactory[Def2_RetrieveUkPropertyPeriodSummaryResponse, RetrieveUkPropertyPeriodSummaryHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveUkPropertyPeriodSummaryHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendUkPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId),
        retrieveUkPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId, self = true),
        listPropertyPeriodSummaries(appConfig, nino, businessId, taxYear, self = false)
      )
    }

  }
}

case class RetrieveUkPropertyPeriodSummaryHateoasData(nino: String, businessId: String, taxYear: String, submissionId: String) extends HateoasData
