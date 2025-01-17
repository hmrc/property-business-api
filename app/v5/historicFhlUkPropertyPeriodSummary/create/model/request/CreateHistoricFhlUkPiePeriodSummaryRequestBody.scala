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

package v5.historicFhlUkPropertyPeriodSummary.create.model.request

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites, Reads, __}
import shapeless.HNil
import shared.utils.EmptinessChecker
import v5.historicFhlUkPropertyPeriodSummary.create.def1.model.request.{UkFhlPropertyExpenses, UkFhlPropertyIncome}

sealed trait CreateHistoricFhlUkPiePeriodSummaryRequestBody {
  val fromDate: String
  val toDate: String
}

case class Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody(
    fromDate: String,
    toDate: String,
    income: Option[UkFhlPropertyIncome],
    expenses: Option[UkFhlPropertyExpenses]
) extends CreateHistoricFhlUkPiePeriodSummaryRequestBody

object Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    "income"     -> body.income ::
      "expenses" -> body.expenses :: HNil
  }

  implicit val reads: Reads[Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody] = (
    (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
      (__ \ "income").readNullable[UkFhlPropertyIncome] and
      (__ \ "expenses").readNullable[UkFhlPropertyExpenses]
  )(Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody.apply _)

  implicit val writes: OWrites[Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody] = (
    (JsPath \ "from").write[String] and
      (JsPath \ "to").write[String] and
      (JsPath \ "financials" \ "incomes").writeNullable[UkFhlPropertyIncome] and
      (JsPath \ "financials" \ "deductions").writeNullable[UkFhlPropertyExpenses]
  )(unlift(Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody.unapply))

}
