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

package v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse

import config.AppConfig
import play.api.libs.json.{ Json, OWrites, Reads }
import v2.hateoas.HateoasLinksFactory
import v2.models.hateoas.{ HateoasData, Link }
import v2.models.response.listPropertyPeriodSummaries.ListPropertyPeriodSummariesResponse.{
  createAmendHistoricNonFhlUkPropertyAnnualSubmission,
  deleteHistoricNonFhlUkPropertyAnnualSubmission,
  retrieveHistoricNonFhlUkPropertyAnnualSubmission
}

case class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse(annualAdjustments: Option[AnnualAdjustments],
                                                                    annualAllowances: Option[AnnualAllowances])

object RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse {
  implicit val writes: OWrites[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse] =
    Json.writes[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse] =
    Json.reads[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]

  implicit object RetrieveHistoricNonFhlUkPropertyAnnualSubmissionLinksFactory
      extends HateoasLinksFactory[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse,
                                  RetrieveHistoricNonFhlUkPropertyAnnualSubmissionHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionHateoasData): Seq[Link] = {
      import data._
      Seq(
        createAmendHistoricNonFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear),
        retrieveHistoricNonFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear, self = true),
        deleteHistoricNonFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear)
      )
    }
  }
}

case class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionHateoasData(nino: String, taxYear: String) extends HateoasData
