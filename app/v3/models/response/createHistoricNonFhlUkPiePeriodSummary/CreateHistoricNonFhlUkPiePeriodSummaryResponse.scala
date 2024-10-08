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

package v3.models.response.createHistoricNonFhlUkPiePeriodSummary

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import api.models.domain.PeriodId
import config.AppConfig
import play.api.libs.json.{Json, OWrites}
import v3.hateoas.HateoasLinks

case class CreateHistoricNonFhlUkPiePeriodSummaryResponse(periodId: PeriodId)

object CreateHistoricNonFhlUkPiePeriodSummaryResponse extends HateoasLinks {

  implicit val writes: OWrites[CreateHistoricNonFhlUkPiePeriodSummaryResponse] = Json.writes

  implicit object LinksFactory
      extends HateoasLinksFactory[CreateHistoricNonFhlUkPiePeriodSummaryResponse, CreateHistoricNonFhlUkPiePeriodSummaryHateoasData] {

    override def links(appConfig: AppConfig, data: CreateHistoricNonFhlUkPiePeriodSummaryHateoasData): Seq[Link] = {
      import data._
      List(
        retrieveHistoricNonFhlUkPiePeriodSummary(appConfig, nino, periodId.value),
        amendHistoricNonFhlUkPiePeriodSummary(appConfig, nino, periodId.value)
      )
    }

  }

}

case class CreateHistoricNonFhlUkPiePeriodSummaryHateoasData(nino: String, periodId: PeriodId) extends HateoasData
