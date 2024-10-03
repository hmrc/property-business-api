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

package v3.models.response.listPropertyPeriodSummaries

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import config.AppConfig
import play.api.libs.json.{Json, OWrites, Reads}
import v3.hateoas.HateoasLinks

case class ListPropertyPeriodSummariesResponse(submissions: Seq[SubmissionPeriod])

object ListPropertyPeriodSummariesResponse extends HateoasLinks {

  implicit def reads: Reads[ListPropertyPeriodSummariesResponse] =
    implicitly[Reads[Seq[SubmissionPeriod]]].map(ListPropertyPeriodSummariesResponse(_))

  implicit def writes: OWrites[ListPropertyPeriodSummariesResponse] =
    Json.writes[ListPropertyPeriodSummariesResponse]

  implicit object LinksFactory extends HateoasLinksFactory[ListPropertyPeriodSummariesResponse, ListPropertyPeriodSummariesHateoasData] {

    override def links(appConfig: AppConfig, data: ListPropertyPeriodSummariesHateoasData): Seq[Link] = {
      import data._
      List(
        listPropertyPeriodSummaries(appConfig, nino = nino, businessId = businessId, taxYear = taxYear, self = true)
      )
    }

  }

}

case class ListPropertyPeriodSummariesHateoasData(nino: String, businessId: String, taxYear: String) extends HateoasData
