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

package v1.models.response.listForeignProperties

import cats.Functor
import config.AppConfig
import play.api.libs.json.{Json, OWrites, Reads, Writes}
import v1.hateoas.{HateoasLinks, HateoasListLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class ListForeignPropertiesResponse[I](submissions: Seq[I])

object ListForeignPropertiesResponse extends HateoasLinks {

  implicit def reads: Reads[ListForeignPropertiesResponse[SubmissionPeriod]] =
    implicitly[Reads[Seq[SubmissionPeriod]]].map(ListForeignPropertiesResponse(_))

  implicit def writes[I: Writes]: OWrites[ListForeignPropertiesResponse[I]] = Json.writes[ListForeignPropertiesResponse[I]]

  implicit object LinksFactory extends HateoasListLinksFactory[ListForeignPropertiesResponse, SubmissionPeriod, ListForeignPropertiesHateoasData] {

    override def itemLinks(appConfig: AppConfig, data: ListForeignPropertiesHateoasData, item: SubmissionPeriod): Seq[Link] =
      Seq(retrieveForeignProperty(appConfig, data.nino, data.businessId, item.submissionId))

    override def links(appConfig: AppConfig, data: ListForeignPropertiesHateoasData): Seq[Link] = {
      Seq(
        listForeignProperties(appConfig, data.nino, data.businessId),
        createForeignProperty(appConfig, data.nino, data.businessId)
      )
    }
  }

  implicit object ResponseFunctor extends Functor[ListForeignPropertiesResponse] {
    override def map[A, B](fa: ListForeignPropertiesResponse[A])(f: A => B): ListForeignPropertiesResponse[B] =
      ListForeignPropertiesResponse(fa.submissions.map(f))
  }
}

case class ListForeignPropertiesHateoasData(nino: String, businessId: String) extends HateoasData
