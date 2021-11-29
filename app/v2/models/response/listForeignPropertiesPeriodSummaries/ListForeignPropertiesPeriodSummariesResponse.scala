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

package v2.models.response.listForeignPropertiesPeriodSummaries

import cats.Functor
import config.AppConfig
import play.api.libs.json.{Json, OWrites, Reads, Writes}
import v2.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v2.models.hateoas.{HateoasData, Link}

case class ListForeignPropertiesPeriodSummariesResponse[I](submissions: Seq[I])

object ListForeignPropertiesPeriodSummariesResponse extends HateoasLinks {

  implicit def reads: Reads[ListForeignPropertiesPeriodSummariesResponse[SubmissionPeriod]] =
    implicitly[Reads[Seq[SubmissionPeriod]]].map(ListForeignPropertiesPeriodSummariesResponse(_))

  implicit def writes[I: Writes]: OWrites[ListForeignPropertiesPeriodSummariesResponse[I]] = Json.writes[ListForeignPropertiesPeriodSummariesResponse[I]]

  implicit object LinksFactory extends HateoasListLinksFactory[ListForeignPropertiesPeriodSummariesResponse, SubmissionPeriod, ListForeignPropertiesPeriodSummariesHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: ListForeignPropertiesPeriodSummariesHateoasData, item: SubmissionPeriod): Seq[Link] = Nil

    override def links(appConfig: AppConfig, data: ListForeignPropertiesPeriodSummariesHateoasData): Seq[Link] = {
      import data._
      val taxYear = "XXXXXXXXXXXXXX" // FIXME remove once (corresponding generic) build endpoint
      Seq(
        listPropertyPeriodSummaries(appConfig, nino, data.businessId, taxYear, self = true)
      )
    }
  }

  implicit object ResponseFunctor extends Functor[ListForeignPropertiesPeriodSummariesResponse] {
    override def map[A, B](fa: ListForeignPropertiesPeriodSummariesResponse[A])(f: A => B): ListForeignPropertiesPeriodSummariesResponse[B] =
      ListForeignPropertiesPeriodSummariesResponse(fa.submissions.map(f))
  }
}

case class ListForeignPropertiesPeriodSummariesHateoasData(nino: String, businessId: String) extends HateoasData
