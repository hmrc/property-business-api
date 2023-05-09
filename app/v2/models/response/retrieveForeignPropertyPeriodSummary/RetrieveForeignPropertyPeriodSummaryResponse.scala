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

package v2.models.response.retrieveForeignPropertyPeriodSummary

import api.hateoas.HateoasLinksFactory
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v2.hateoas.HateoasLinks
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea.ForeignFhlEea
import v2.models.response.retrieveForeignPropertyPeriodSummary.foreignNonFhlProperty.ForeignNonFhlProperty

case class RetrieveForeignPropertyPeriodSummaryResponse(submittedOn: String,
                                                        fromDate: String,
                                                        toDate: String,
                                                        foreignFhlEea: Option[ForeignFhlEea],
                                                        foreignNonFhlProperty: Option[Seq[ForeignNonFhlProperty]])

object RetrieveForeignPropertyPeriodSummaryResponse extends HateoasLinks {
  implicit val writes: OWrites[RetrieveForeignPropertyPeriodSummaryResponse] = Json.writes[RetrieveForeignPropertyPeriodSummaryResponse]

  implicit val reads: Reads[RetrieveForeignPropertyPeriodSummaryResponse] = (
    (JsPath \ "submittedOn").read[String] and
      (JsPath \ "fromDate").read[String] and
      (JsPath \ "toDate").read[String] and
      (JsPath \ "foreignFhlEea").readNullable[ForeignFhlEea] and
      (JsPath \ "foreignProperty").readNullable[Seq[ForeignNonFhlProperty]]
  )(RetrieveForeignPropertyPeriodSummaryResponse.apply _)

  implicit object RetrieveForeignPropertyLinksFactory
      extends HateoasLinksFactory[RetrieveForeignPropertyPeriodSummaryResponse, RetrieveForeignPropertyPeriodSummaryHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveForeignPropertyPeriodSummaryHateoasData): Seq[Link] = {
      import data._

      Seq(
        amendForeignPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId),
        retrieveForeignPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId, self = true),
        listPropertyPeriodSummaries(appConfig, nino, businessId, taxYear, self = false)
      )
    }

  }

}

case class RetrieveForeignPropertyPeriodSummaryHateoasData(nino: String, businessId: String, taxYear: String, submissionId: String)
    extends HateoasData
