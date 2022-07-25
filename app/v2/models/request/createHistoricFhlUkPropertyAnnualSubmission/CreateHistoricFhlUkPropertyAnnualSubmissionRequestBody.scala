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

package v2.models.request.createHistoricFhlUkPropertyAnnualSubmission

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shapeless.HNil
import utils.EmptinessChecker
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission.{HistoricFhlAnnualAdjustments, HistoricFhlAnnualAllowances}

case class CreateHistoricFhlUkPropertyAnnualSubmissionRequestBody(annualAdjustments: Option[HistoricFhlAnnualAdjustments],
                                                                  annualAllowances: Option[HistoricFhlAnnualAllowances])

object CreateHistoricFhlUkPropertyAnnualSubmissionRequestBody {
  implicit val emptinessChecker: EmptinessChecker[CreateHistoricFhlUkPropertyAnnualSubmissionRequestBody] = EmptinessChecker.use { body =>
    "annualAdjustments" -> body.annualAdjustments ::
    "annualAllowances" -> body.annualAllowances :: HNil
  }

  implicit val reads: Reads[CreateHistoricFhlUkPropertyAnnualSubmissionRequestBody] = Json.reads[CreateHistoricFhlUkPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[CreateHistoricFhlUkPropertyAnnualSubmissionRequestBody] = (
    (JsPath \ "annualAdjustments").writeNullable[HistoricFhlAnnualAdjustments] and
      (JsPath \ "annualAllowances").writeNullable[HistoricFhlAnnualAllowances]
    )(unlift(CreateHistoricFhlUkPropertyAnnualSubmissionRequestBody.unapply))
}
