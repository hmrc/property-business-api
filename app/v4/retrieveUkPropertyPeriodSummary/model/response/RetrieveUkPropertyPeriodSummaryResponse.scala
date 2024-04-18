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

package v4.retrieveUkPropertyPeriodSummary.model.response

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import api.models.domain.Timestamp
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OWrites, Reads, __}
import v4.retrieveUkPropertyPeriodSummary.def1.model.response.{Def1_Retrieve_UkFhlProperty, Def1_Retrieve_UkNonFhlProperty}
import v4.retrieveUkPropertyPeriodSummary.def2.model.response.{Def2_Retrieve_ConsolidatedUkFhlProperty, Def2_Retrieve_ConsolidatedUkNonFhlProperty, Def2_Retrieve_UkFhlProperty, Def2_Retrieve_UkNonFhlProperty}
import v4.retrieveUkPropertyPeriodSummary.model.response.Def1_RetrieveUkPropertyPeriodSummaryResponse.Def1_RetrieveUkPropertyPeriodSummaryResponseLinksFactory
import v4.hateoas.HateoasLinks

sealed trait RetrieveUkPropertyPeriodSummaryResponse

object RetrieveUkPropertyPeriodSummaryResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveUkPropertyPeriodSummaryResponse] = {
    case def1: Def1_RetrieveUkPropertyPeriodSummaryResponse                         => Json.toJsObject(def1)
    case def2: Def2_RetrieveUkPropertyPeriodSummaryResponse                         => Json.toJsObject(def2)
    case def2Consolidated: Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse => Json.toJsObject(def2Consolidated)
  }

  implicit object RetrieveUkPropertyPeriodSummaryResponseLinksFactory
      extends HateoasLinksFactory[RetrieveUkPropertyPeriodSummaryResponse, RetrieveUkPropertyPeriodSummaryHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveUkPropertyPeriodSummaryHateoasData): Seq[Link] = {
      Def1_RetrieveUkPropertyPeriodSummaryResponseLinksFactory.links(appConfig, data)
    }

  }

}

case class Def1_RetrieveUkPropertyPeriodSummaryResponse(submittedOn: Timestamp,
                                                        fromDate: String,
                                                        toDate: String,
                                                        // periodCreationDate: Option[String], // To be reinstated, see MTDSA-15575
                                                        ukFhlProperty: Option[Def1_Retrieve_UkFhlProperty],
                                                        ukNonFhlProperty: Option[Def1_Retrieve_UkNonFhlProperty])
    extends RetrieveUkPropertyPeriodSummaryResponse

object Def1_RetrieveUkPropertyPeriodSummaryResponse extends HateoasLinks {

  implicit val writes: OWrites[Def1_RetrieveUkPropertyPeriodSummaryResponse] = Json.writes[Def1_RetrieveUkPropertyPeriodSummaryResponse]

  implicit val reads: Reads[Def1_RetrieveUkPropertyPeriodSummaryResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
//      (__ \ "periodCreationDate").readNullable[String] and // To be reinstated, see MTDSA-15575
      (__ \ "ukFhlProperty").readNullable[Def1_Retrieve_UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[Def1_Retrieve_UkNonFhlProperty]
  )(Def1_RetrieveUkPropertyPeriodSummaryResponse.apply _)

  implicit object Def1_RetrieveUkPropertyPeriodSummaryResponseLinksFactory
      extends HateoasLinksFactory[Def1_RetrieveUkPropertyPeriodSummaryResponse, RetrieveUkPropertyPeriodSummaryHateoasData] {

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

case class Def2_RetrieveUkPropertyPeriodSummaryResponse(submittedOn: Timestamp,
                                                        fromDate: String,
                                                        toDate: String,
                                                        // periodCreationDate: Option[String], // To be reinstated, see MTDSA-15575
                                                        ukFhlProperty: Option[Def2_Retrieve_UkFhlProperty],
                                                        ukNonFhlProperty: Option[Def2_Retrieve_UkNonFhlProperty])
    extends RetrieveUkPropertyPeriodSummaryResponse

object Def2_RetrieveUkPropertyPeriodSummaryResponse extends HateoasLinks {

  implicit val writes: OWrites[Def2_RetrieveUkPropertyPeriodSummaryResponse] = Json.writes[Def2_RetrieveUkPropertyPeriodSummaryResponse]

  implicit val reads: Reads[Def2_RetrieveUkPropertyPeriodSummaryResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
      //      (__ \ "periodCreationDate").readNullable[String] and // To be reinstated, see MTDSA-15575
      (__ \ "ukFhlProperty").readNullable[Def2_Retrieve_UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[Def2_Retrieve_UkNonFhlProperty]
  )(Def2_RetrieveUkPropertyPeriodSummaryResponse.apply _)

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

case class Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse(submittedOn: Timestamp,
                                                                    fromDate: String,
                                                                    toDate: String,
                                                                    // periodCreationDate: Option[String], // To be reinstated, see MTDSA-15575
                                                                    ukFhlProperty: Option[Def2_Retrieve_ConsolidatedUkFhlProperty],
                                                                    ukNonFhlProperty: Option[Def2_Retrieve_ConsolidatedUkNonFhlProperty])
    extends RetrieveUkPropertyPeriodSummaryResponse

object Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse extends HateoasLinks {

  implicit val writes: OWrites[Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse] =
    Json.writes[Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse]

  implicit val reads: Reads[Def2_RetrieveUkPropertyPeriodSummaryConsolidatedResponse] = (
    (__ \ "submittedOn").read[Timestamp] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
      //      (__ \ "periodCreationDate").readNullable[String] and // To be reinstated, see MTDSA-15575
      (__ \ "ukFhlProperty").readNullable[Def2_Retrieve_ConsolidatedUkFhlProperty] and
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
