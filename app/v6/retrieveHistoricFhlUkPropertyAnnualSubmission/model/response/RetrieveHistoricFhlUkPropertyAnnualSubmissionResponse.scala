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

package v6.retrieveHistoricFhlUkPropertyAnnualSubmission.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v6.retrieveHistoricFhlUkPropertyAnnualSubmission.def1.model.response._

sealed trait RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse

object RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse] = {
    case def1: Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse => Json.toJsObject(def1)
  }

}

case class Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse(annualAdjustments: Option[AnnualAdjustments],
                                                                      annualAllowances: Option[AnnualAllowances])
    extends RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse

object Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse {

  implicit val writes: OWrites[Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse] =
    Json.writes[Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]

  implicit val reads: Reads[Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse] =
    ((JsPath \ "annualAdjustments").readNullable[AnnualAdjustments] and
      (JsPath \ "annualAllowances").readNullable[AnnualAllowances])(Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse.apply _)

}
