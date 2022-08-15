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

package v2.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission

import config.AppConfig
import play.api.libs.json.{ Json, OWrites, Reads }
import v2.hateoas.{ HateoasLinks, HateoasLinksFactory }
import v2.models.hateoas.{ HateoasData, Link }

case class RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse(annualAdjustments: Option[AnnualAdjustments],
                                                                 annualAllowances: Option[AnnualAllowances])

object RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse extends HateoasLinks {
  implicit val writes: OWrites[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse] =
    Json.writes[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse] =
    Json.reads[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]

  implicit object RetrieveHistoricFhlUkPropertyAnnualSubmissionLinksFactory
      extends HateoasLinksFactory[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse, RetrieveHistoricFhlUkPropertyAnnualSubmissionHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveHistoricFhlUkPropertyAnnualSubmissionHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveHistoricFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear),
      )
    }
  }

}

case class RetrieveHistoricFhlUkPropertyAnnualSubmissionHateoasData(nino: String, taxYear: String) extends HateoasData
