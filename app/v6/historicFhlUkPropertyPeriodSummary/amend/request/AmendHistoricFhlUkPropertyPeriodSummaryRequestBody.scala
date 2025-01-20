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

package v6.historicFhlUkPropertyPeriodSummary.amend.request

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v6.historicFhlUkPropertyPeriodSummary.amend.def1.model.request._

sealed trait AmendHistoricFhlUkPropertyPeriodSummaryRequestBody

case class Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody(income: Option[UkFhlPropertyIncome], expenses: Option[UkFhlPropertyExpenses])
    extends AmendHistoricFhlUkPropertyPeriodSummaryRequestBody

object Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody {

  implicit val reads: Reads[Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody] =
    Json.reads[Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "incomes").writeNullable[UkFhlPropertyIncome] and
      (JsPath \ "deductions").writeNullable[UkFhlPropertyExpenses]
  )(unlift(Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody.unapply))

}
