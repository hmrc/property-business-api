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

package v1.models.response.createForeignPropertyPeriodSummary

import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v1.hateoas.{HateoasLinksFactory, HateoasLinks}
import v1.models.hateoas.{HateoasData, Link}

case class CreateForeignPropertyPeriodSummaryResponse(submissionId: String)

object CreateForeignPropertyPeriodSummaryResponse extends HateoasLinks{
  implicit val format: OFormat[CreateForeignPropertyPeriodSummaryResponse] = Json.format[CreateForeignPropertyPeriodSummaryResponse]

  implicit object LinksFactory extends HateoasLinksFactory[CreateForeignPropertyPeriodSummaryResponse, CreateForeignPropertyPeriodSummaryHateoasData] {
    override def links(appConfig: AppConfig, data: CreateForeignPropertyPeriodSummaryHateoasData): Seq[Link] = {
      import data._
      Seq(retrieveForeignPropertyPeriodSummary(appConfig, nino, businessId, submissionId))
    }
  }
}

case class CreateForeignPropertyPeriodSummaryHateoasData(nino: String, businessId: String, submissionId: String) extends HateoasData