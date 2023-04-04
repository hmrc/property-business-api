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

package v2.models.response.retrieveUkPropertyPeriodSummary

import api.hateoas.HateoasLinksFactory
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Json, OWrites, Reads}
import v2.hateoas.HateoasLinks

case class RetrieveUkPropertyPeriodSummaryResponse(submittedOn: String,
                                                   fromDate: String,
                                                   toDate: String,
//                                                   periodCreationDate: Option[String], // To be reinstated, see MTDSA-15575
                                                   ukFhlProperty: Option[UkFhlProperty],
                                                   ukNonFhlProperty: Option[UkNonFhlProperty])

object RetrieveUkPropertyPeriodSummaryResponse extends HateoasLinks {
  implicit val writes: OWrites[RetrieveUkPropertyPeriodSummaryResponse] = Json.writes[RetrieveUkPropertyPeriodSummaryResponse]

  implicit val reads: Reads[RetrieveUkPropertyPeriodSummaryResponse] = (
    (__ \ "submittedOn").read[String] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
//      (__ \ "periodCreationDate").readNullable[String] and // To be reinstated, see MTDSA-15575
      (__ \ "ukFhlProperty").readNullable[UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[UkNonFhlProperty]
  )(RetrieveUkPropertyPeriodSummaryResponse.apply _)

  implicit object hateoasLinksFactory
      extends HateoasLinksFactory[RetrieveUkPropertyPeriodSummaryResponse, RetrieveUkPropertyPeriodSummaryHateoasData] {

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
