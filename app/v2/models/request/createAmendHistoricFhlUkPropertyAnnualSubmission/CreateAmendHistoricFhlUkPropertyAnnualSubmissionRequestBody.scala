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

package v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission

import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Json, OWrites, Reads }

case class CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(annualAdjustments: Option[HistoricFhlAnnualAdjustments],
                                                                       annualAllowances: Option[HistoricFhlAnnualAllowances])

object CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody {
  implicit val reads: Reads[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody] =
    Json.reads[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody] = (
    (JsPath \ "annualAdjustments").writeNullable[HistoricFhlAnnualAdjustments] and
      (JsPath \ "annualAllowances").writeNullable[HistoricFhlAnnualAllowances]
  )(unlift(CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody.unapply))
}
