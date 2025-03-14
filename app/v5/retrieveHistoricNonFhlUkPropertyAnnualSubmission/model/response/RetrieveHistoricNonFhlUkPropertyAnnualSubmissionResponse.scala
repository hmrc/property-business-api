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

package v5.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.response

import play.api.libs.json.{Json, OFormat, OWrites}
import v5.retrieveHistoricNonFhlUkPropertyAnnualSubmission.def1.model.response.{AnnualAdjustments, AnnualAllowances}

sealed trait RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse

object RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse] = {
    case def1: Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse => Json.toJsObject(def1)
  }

}

case class Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse(
    annualAdjustments: Option[AnnualAdjustments],
    annualAllowances: Option[AnnualAllowances]
) extends RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse

object Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse {

  implicit val format: OFormat[Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse] =
    Json.format[Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse]

}
