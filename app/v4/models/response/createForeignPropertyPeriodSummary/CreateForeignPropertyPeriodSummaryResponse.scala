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

package v4.models.response.createForeignPropertyPeriodSummary

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v4.hateoas.HateoasLinks

case class CreateForeignPropertyPeriodSummaryResponse(submissionId: String)

object CreateForeignPropertyPeriodSummaryResponse extends HateoasLinks {
  implicit val format: OFormat[CreateForeignPropertyPeriodSummaryResponse] = Json.format[CreateForeignPropertyPeriodSummaryResponse]

  implicit object LinksFactory
      extends HateoasLinksFactory[CreateForeignPropertyPeriodSummaryResponse, CreateForeignPropertyPeriodSummaryHateoasData] {

    override def links(appConfig: AppConfig, data: CreateForeignPropertyPeriodSummaryHateoasData): Seq[Link] = {
      import data._
      List(
        retrieveForeignPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId, self = true),
        amendForeignPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId)
      )
    }

  }

}

case class CreateForeignPropertyPeriodSummaryHateoasData(nino: String, businessId: String, taxYear: String, submissionId: String) extends HateoasData
