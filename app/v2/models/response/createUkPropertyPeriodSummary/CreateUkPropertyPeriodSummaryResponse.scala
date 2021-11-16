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

package v2.models.response.createUkPropertyPeriodSummary

import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v2.hateoas.HateoasLinksFactory
import v2.hateoas.HateoasLinks
import v2.models.hateoas.{HateoasData, Link}

case class CreateUkPropertyPeriodSummaryResponse(submissionId: String)

object CreateUkPropertyPeriodSummaryResponse extends HateoasLinks{
  implicit val format: OFormat[CreateUkPropertyPeriodSummaryResponse] = Json.format[CreateUkPropertyPeriodSummaryResponse]
  
  implicit object LinksFactory extends HateoasLinksFactory[CreateUkPropertyPeriodSummaryResponse, CreateUkPropertyPeriodSummaryHateoasData] {
    override def links(appConfig: AppConfig, data: CreateUkPropertyPeriodSummaryHateoasData): Seq[Link] = {
     import data._
      Seq(retrieveUkPropertyPeriodicSummary(appConfig, nino, businessId, taxYear, submissionId))
    }
  }
}

case class CreateUkPropertyPeriodSummaryHateoasData(nino: String, businessId: String, taxYear: String, submissionId: String) extends HateoasData
