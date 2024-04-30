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

package v4.amendHistoricFhlUkPropertyPeriodSummary.model.request

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v4.amendHistoricFhlUkPropertyPeriodSummary.def1.model.request._


sealed trait AmendHistoricFhlUkPiePeriodSummaryRequestBody

case class Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody(income: Option[UkFhlPieIncome], expenses: Option[UkFhlPieExpenses])
  extends AmendHistoricFhlUkPiePeriodSummaryRequestBody

object Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody {

  implicit val reads: Reads[Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody] = Json.reads[Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody]

  implicit val writes: OWrites[Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody] = (
    (JsPath \ "incomes").writeNullable[UkFhlPieIncome] and
      (JsPath \ "deductions").writeNullable[UkFhlPieExpenses]
  )(unlift(Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody.unapply))

}
