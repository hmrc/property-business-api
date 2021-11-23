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

package v2.models.response.retrieveUkPropertyPeriodSummary

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, OWrites, Reads, __}
import v2.hateoas.{HateoasLinks, HateoasLinksFactory}
import v2.models.hateoas.{HateoasData, Link}

case class RetrieveUkPropertyPeriodSummaryResponse(submittedOn: String,
                                                   fromDate: String,
                                                   toDate: String,
                                                   ukFhlProperty: Option[UkFhlProperty],
                                                   ukNonFhlProperty: Option[UkNonFhlProperty])

object RetrieveUkPropertyPeriodSummaryResponse extends HateoasLinks {
  implicit val writes: OWrites[RetrieveUkPropertyPeriodSummaryResponse] = Json.writes[RetrieveUkPropertyPeriodSummaryResponse]

  implicit val reads: Reads[RetrieveUkPropertyPeriodSummaryResponse] = (
    (__ \ "submittedOn").read[String] and
      (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
      (__ \ "ukFhlProperty").readNullable[UkFhlProperty] and
      (__ \ "ukOtherProperty").readNullable[UkNonFhlProperty]
    ) (RetrieveUkPropertyPeriodSummaryResponse.apply _)

  implicit object hateoasLinksFactory extends HateoasLinksFactory[RetrieveUkPropertyPeriodSummaryResponse, RetrieveUkPropertyPeriodSummaryHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveUkPropertyPeriodSummaryHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendUkPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId),
        retrieveUkPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId),
        listPropertyPeriodicSummary(appConfig, nino, businessId, taxYear)
      )
    }
  }

}

case class RetrieveUkPropertyPeriodSummaryHateoasData(nino: String, businessId: String, taxYear: String, submissionId: String) extends HateoasData