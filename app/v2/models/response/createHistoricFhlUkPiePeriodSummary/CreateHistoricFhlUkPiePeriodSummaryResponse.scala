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

package v2.models.response.createHistoricFhlUkPiePeriodSummary

import config.AppConfig
import play.api.libs.json.{ Json, OFormat }
import v2.hateoas.{ HateoasLinks, HateoasLinksFactory }
import v2.models.hateoas.{ HateoasData, Link }

case class CreateHistoricFhlUkPiePeriodSummaryResponse(transactionReference: String)

object CreateHistoricFhlUkPiePeriodSummaryResponse extends HateoasLinks {
  implicit val format: OFormat[CreateHistoricFhlUkPiePeriodSummaryResponse] = Json.format[CreateHistoricFhlUkPiePeriodSummaryResponse]

  implicit object LinksFactory
      extends HateoasLinksFactory[CreateHistoricFhlUkPiePeriodSummaryResponse, CreateHistoricFhlUkPiePeriodSummaryHateoasData] {
    override def links(appConfig: AppConfig, data: CreateHistoricFhlUkPiePeriodSummaryHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendHistoricFhlUkPiePeriodSubmission(appConfig, nino, periodId),
        retrieveHistoricFhlUkPiePeriodSubmission(appConfig, nino, periodId, self = true)
      )
    }
  }
}

case class CreateHistoricFhlUkPiePeriodSummaryHateoasData(nino: String, periodId: String, transactionId: String) extends HateoasData
