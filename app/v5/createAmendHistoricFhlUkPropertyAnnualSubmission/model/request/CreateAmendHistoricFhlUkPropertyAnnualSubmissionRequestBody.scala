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

package v5.createAmendHistoricFhlUkPropertyAnnualSubmission.model.request

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v5.createAmendHistoricFhlUkPropertyAnnualSubmission.def1.model.request.{HistoricFhlAnnualAdjustments, HistoricFhlAnnualAllowances}

sealed trait CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody

case class Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(annualAdjustments: Option[HistoricFhlAnnualAdjustments],
                                                                            annualAllowances: Option[HistoricFhlAnnualAllowances])
    extends CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody

object Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody {

  implicit val reads: Reads[Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody] =
    Json.reads[Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody]

  implicit val writes: OWrites[Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody] = (
    (JsPath \ "annualAdjustments").writeNullable[HistoricFhlAnnualAdjustments] and
      (JsPath \ "annualAllowances").writeNullable[HistoricFhlAnnualAllowances]
  )(unlift(Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody.unapply))

}
