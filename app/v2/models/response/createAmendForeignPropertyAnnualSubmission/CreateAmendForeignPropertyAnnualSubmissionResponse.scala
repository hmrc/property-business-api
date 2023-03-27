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

package v2.models.response.createAmendForeignPropertyAnnualSubmission

import config.AppConfig
import v2.hateoas.HateoasLinks
import api.hateoas.HateoasLinksFactory
import api.models.hateoas.{HateoasData, Link}

object CreateAmendForeignPropertyAnnualSubmissionResponse extends HateoasLinks {

  implicit object LinksFactory extends HateoasLinksFactory[Unit, CreateAmendForeignPropertyAnnualSubmissionHateoasData] {
    override def links(appConfig: AppConfig, data: CreateAmendForeignPropertyAnnualSubmissionHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendForeignPropertyAnnualSubmission(appConfig, nino, businessId, taxYear),
        retrieveForeignPropertyAnnualSubmission(appConfig, nino, businessId, taxYear, self = true),
        deletePropertyAnnualSubmission(appConfig, nino, businessId, taxYear)
      )
    }
  }
}

case class CreateAmendForeignPropertyAnnualSubmissionHateoasData(nino: String, businessId: String, taxYear: String) extends HateoasData
