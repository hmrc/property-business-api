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

package v2.models.response.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import config.AppConfig
import play.api.libs.json.{ Json, OWrites, Reads }
import v2.hateoas.{ HateoasLinks, HateoasLinksFactory }
import v2.models.hateoas.{ HateoasData, Link }

case class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(transactionReference: Option[String])

object CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse extends HateoasLinks {
  implicit val reads: Reads[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse] =
    Json.reads[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse]

  implicit val writes: OWrites[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse] =
    (_: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse) => Json.obj()

  implicit object CreateAmendHistoricNonFhlUkPropertyLinksFactory
      extends HateoasLinksFactory[CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse,
                                  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData] {
    override def links(appConfig: AppConfig, data: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveHistoricNonFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear, self = true),
        createAmendHistoricNonFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear),
        deleteHistoricNonFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear)
      )
    }
  }
}

case class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionHateoasData(nino: String, taxYear: String) extends HateoasData
