/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.models.response.createHistoricNonFhlUkPiePeriodSummaryResponse

import config.AppConfig
import play.api.libs.json.{Json, OFormat}
import v2.hateoas.{HateoasLinksFactory, HateoasLinks}
import v2.models.hateoas.{HateoasData, Link}

case class CreateHistoricNonFhlUkPiePeriodSummaryResponse(periodId: String)

object CreateHistoricNonFhlUkPiePeriodSummaryResponse extends HateoasLinks{
  implicit val format: OFormat[CreateHistoricNonFhlUkPiePeriodSummaryResponse] = Json.format[CreateHistoricNonFhlUkPiePeriodSummaryResponse]

  implicit object LinksFactory extends HateoasLinksFactory[CreateHistoricNonFhlUkPiePeriodSummaryResponse, CreateHistoricNonFhlUkPiePeriodSummaryHateoasData] {
    override def links(appConfig: AppConfig, data: CreateHistoricNonFhlUkPiePeriodSummaryHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendHistoricNonFhlUkPiePeriodSummary(appConfig: AppConfig, nino: String, periodId: String)
      )
    }
  }
}

case class CreateHistoricNonFhlUkPiePeriodSummaryHateoasData(nino: String, periodId: String) extends HateoasData