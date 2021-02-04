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

package v1.models.response.amendForeignPropertyPeriodSummary

import config.AppConfig
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}
import v1.models.hateoas.RelType._

object AmendForeignPropertyPeriodSummaryResponse extends HateoasLinks {

  implicit object AmendForeignPropertyLinksFactory extends HateoasLinksFactory[Unit, AmendForeignPropertyPeriodSummaryHateoasData] {
    override def links(appConfig: AppConfig, data: AmendForeignPropertyPeriodSummaryHateoasData): Seq[Link] = {
      import data._
      Seq(
        amendForeignPropertyPeriodSummary(appConfig, nino, businessId, submissionId),
        retrieveForeignPropertyPeriodSummary(appConfig, nino, businessId, submissionId),
        listForeignPropertiesPeriodSummaries(appConfig, nino, businessId, LIST_PROPERTY_PERIOD_SUMMARIES)
      )
    }
  }
}


case class AmendForeignPropertyPeriodSummaryHateoasData(nino: String, businessId: String, submissionId: String) extends HateoasData