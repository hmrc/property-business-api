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

package v2.models.response.createAmendHistoricFhlUkPropertyAnnualSubmission

import api.hateoas.HateoasLinksFactory
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{Json, OWrites, Reads}
import v2.hateoas.HateoasLinks

case class CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(transactionReference: Option[String])

object CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse extends HateoasLinks {

  implicit val reads: Reads[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse] =
    Json.reads[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse]

  implicit val writes: OWrites[CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse] =
    (_: CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse) => Json.obj()

  implicit object CreateAmendHistoricFhlUkPropertyLinksFactory
      extends HateoasLinksFactory[
        CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse,
        CreateAmendHistoricFhlUkPropertyAnnualSubmissionHateoasData] {

    override def links(appConfig: AppConfig, data: CreateAmendHistoricFhlUkPropertyAnnualSubmissionHateoasData): Seq[Link] = {
      import data._

      Seq(
        retrieveHistoricFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear, self = true),
        createAmendHistoricFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear),
        deleteHistoricFhlUkPropertyAnnualSubmission(appConfig, nino, taxYear)
      )

    }

  }

}

case class CreateAmendHistoricFhlUkPropertyAnnualSubmissionHateoasData(nino: String, taxYear: String) extends HateoasData
