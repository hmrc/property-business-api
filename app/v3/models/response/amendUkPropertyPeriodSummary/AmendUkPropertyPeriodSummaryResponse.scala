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

package v3.models.response.amendUkPropertyPeriodSummary

import api.hateoas.{HateoasData, HateoasLinksFactory, Link}
import config.AppConfig
import v3.hateoas.HateoasLinks

case class AmendUkPropertyPeriodSummaryResponse(submissionId: String)

object AmendUkPropertyPeriodSummaryResponse extends HateoasLinks {

  implicit object AmendUkPropertyLinksFactory extends HateoasLinksFactory[Unit, AmendUkPropertyPeriodSummaryHateoasData] {

    override def links(appConfig: AppConfig, data: AmendUkPropertyPeriodSummaryHateoasData): Seq[Link] = {
      import data._
      List(
        listPropertyPeriodSummaries(appConfig, nino, businessId, taxYear, self = false),
        retrieveUkPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId, self = true),
        amendUkPropertyPeriodSummary(appConfig, nino, businessId, taxYear, submissionId)
      )
    }

  }

}

case class AmendUkPropertyPeriodSummaryHateoasData(nino: String, businessId: String, taxYear: String, submissionId: String) extends HateoasData
