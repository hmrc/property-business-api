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

package v2.models.response.amendHistoricNonFhlUkPiePeriodSummary

import config.AppConfig
import v2.hateoas.HateoasLinks
import api.hateoas.HateoasLinksFactory
import api.models.hateoas.{HateoasData, Link}

object AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData extends HateoasLinks {
  implicit object LinksFactory extends HateoasLinksFactory[Unit, AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData] {
    override def links(appConfig: AppConfig, data: AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendHistoricNonFhlUkPiePeriodSummary(appConfig, nino, periodId),
        retrieveHistoricNonFhlUkPiePeriodSummary(appConfig, nino, periodId),
        listHistoricNonFhlUkPiePeriodSummaries(appConfig, nino, self = false)
      )
    }
  }
}

case class AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData(nino: String, periodId: String) extends HateoasData
