/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.retrieveForeignPropertyPeriodSummary

import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}
import v1.models.hateoas.RelType.LIST_PROPERTY_PERIOD_SUMMARIES
import v1.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea.ForeignFhlEea
import v1.models.response.retrieveForeignPropertyPeriodSummary.foreignProperty.ForeignProperty

case class RetrieveForeignPropertyResponse(fromDate: String,
                                           toDate: String,
                                           foreignFhlEea: Option[ForeignFhlEea],
                                           foreignProperty: Option[Seq[ForeignProperty]])

object RetrieveForeignPropertyResponse extends HateoasLinks {
  implicit val format: OFormat[RetrieveForeignPropertyResponse] = Json.format[RetrieveForeignPropertyResponse]

  implicit object RetrieveForeignPropertyLinksFactory extends HateoasLinksFactory[RetrieveForeignPropertyResponse, RetrieveForeignPropertyHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveForeignPropertyHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendForeignPropertyPeriodSummary(appConfig, nino, businessId, submissionId),
        retrieveForeignPropertyPeriodSummary(appConfig, nino, businessId, submissionId),
        listForeignPropertiesPeriodSummaries(appConfig, nino, businessId, rel = LIST_PROPERTY_PERIOD_SUMMARIES)
      )
    }
  }
}

case class RetrieveForeignPropertyHateoasData(nino: String, businessId: String, submissionId: String) extends HateoasData