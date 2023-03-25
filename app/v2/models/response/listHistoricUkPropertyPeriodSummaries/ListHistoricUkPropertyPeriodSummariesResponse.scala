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

package v2.models.response.listHistoricUkPropertyPeriodSummaries

import api.hateoas.HateoasListLinksFactory
import api.models.hateoas.{HateoasData, Link}
import cats.Functor
import config.AppConfig
import play.api.libs.json.{__, Json, OWrites, Reads, Writes}
import v2.hateoas.HateoasLinks
import v2.models.domain.HistoricPropertyType

case class ListHistoricUkPropertyPeriodSummariesResponse[I](submissions: Seq[I])

object ListHistoricUkPropertyPeriodSummariesResponse extends HateoasLinks {

  implicit def reads[I: Reads]: Reads[ListHistoricUkPropertyPeriodSummariesResponse[I]] =
    (__ \ "periods").read[List[I]].map(ListHistoricUkPropertyPeriodSummariesResponse(_))

  implicit def writes[I: Writes]: OWrites[ListHistoricUkPropertyPeriodSummariesResponse[I]] = Json.writes

  implicit object LinksFactory
      extends HateoasListLinksFactory[ListHistoricUkPropertyPeriodSummariesResponse,
                                      SubmissionPeriod,
                                      ListHistoricUkPropertyPeriodSummariesHateoasData] {
    override def itemLinks(appConfig: AppConfig, data: ListHistoricUkPropertyPeriodSummariesHateoasData, item: SubmissionPeriod): Seq[Link] = {
      import data._

      data.propertyType match {
        case HistoricPropertyType.Fhl =>
          Seq(
            amendHistoricFhlUkPiePeriodSummary(appConfig, nino, item.periodId.value),
            retrieveHistoricFhlUkPiePeriodSummary(appConfig, nino, item.periodId.value)
          )
        case HistoricPropertyType.NonFhl =>
          Seq(
            amendHistoricNonFhlUkPiePeriodSummary(appConfig, nino, item.periodId.value),
            retrieveHistoricNonFhlUkPiePeriodSummary(appConfig, nino, item.periodId.value)
          )
      }
    }

    override def links(appConfig: AppConfig, data: ListHistoricUkPropertyPeriodSummariesHateoasData): Seq[Link] = {
      import data._

      data.propertyType match {
        case HistoricPropertyType.Fhl =>
          Seq(
            listHistoricFhlUkPiePeriodSummaries(appConfig, nino, self = true),
            createHistoricFhlUkPiePeriodSummary(appConfig, nino)
          )
        case HistoricPropertyType.NonFhl =>
          Seq(
            listHistoricNonFhlUkPiePeriodSummaries(appConfig, nino, self = true),
            createHistoricNonFhlUkPiePeriodSummary(appConfig, nino)
          )
      }
    }
  }

  implicit object ResponseFunctor extends Functor[ListHistoricUkPropertyPeriodSummariesResponse] {
    override def map[A, B](fa: ListHistoricUkPropertyPeriodSummariesResponse[A])(f: A => B): ListHistoricUkPropertyPeriodSummariesResponse[B] =
      ListHistoricUkPropertyPeriodSummariesResponse(fa.submissions.map(f))
  }
}

case class ListHistoricUkPropertyPeriodSummariesHateoasData(nino: String, propertyType: HistoricPropertyType) extends HateoasData
