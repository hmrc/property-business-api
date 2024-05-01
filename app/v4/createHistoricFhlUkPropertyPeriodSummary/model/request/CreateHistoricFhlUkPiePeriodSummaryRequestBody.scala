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

package v4.createHistoricFhlUkPropertyPeriodSummary.model.request

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites, Reads, __}
import shapeless.HNil
import utils.EmptinessChecker
import v4.createHistoricFhlUkPropertyPeriodSummary.def1.model.request.{UkFhlPieExpenses, UkFhlPieIncome}



sealed trait CreateHistoricFhlUkPiePeriodSummaryRequestBody

case class Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody(fromDate: String,
                                                          toDate: String,
                                                          income: Option[UkFhlPieIncome],
                                                          expenses: Option[UkFhlPieExpenses])
extends CreateHistoricFhlUkPiePeriodSummaryRequestBody

object Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    "income"     -> body.income ::
      "expenses" -> body.expenses :: HNil
  }

  implicit val reads: Reads[Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody] = (
    (__ \ "fromDate").read[String] and
      (__ \ "toDate").read[String] and
      (__ \ "income").readNullable[UkFhlPieIncome] and
      (__ \ "expenses").readNullable[UkFhlPieExpenses]
    )(Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody.apply _)

  implicit val writes: OWrites[Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody] = (
    (JsPath \ "from").write[String] and
      (JsPath \ "to").write[String] and
      (JsPath \ "financials" \ "incomes").writeNullable[UkFhlPieIncome] and
      (JsPath \ "financials" \ "deductions").writeNullable[UkFhlPieExpenses]
    )(unlift(Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody.unapply))

}
