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

package v5.historicNonFhlUkPropertyPeriodSummary.create.model.request

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shared.utils.EmptinessChecker
import shared.utils.EmptinessChecker.field
import v5.historicNonFhlUkPropertyPeriodSummary.create.def1.model.request.{UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}

sealed trait CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody {
  val fromDate: String
  val toDate: String
}

case class Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
    fromDate: String,
    toDate: String,
    income: Option[UkNonFhlPropertyIncome],
    expenses: Option[UkNonFhlPropertyExpenses]
) extends CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody

object Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    List(
      field("income", body.income),
      field("expenses", body.expenses)
    )
  }

  implicit val reads: Reads[Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody] =
    Json.reads[Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "from").write[String] and
      (JsPath \ "to").write[String] and
      (JsPath \ "financials" \ "incomes").writeNullable[UkNonFhlPropertyIncome] and
      (JsPath \ "financials" \ "deductions").writeNullable[UkNonFhlPropertyExpenses]
  )(o => Tuple.fromProductTyped(o))

}
