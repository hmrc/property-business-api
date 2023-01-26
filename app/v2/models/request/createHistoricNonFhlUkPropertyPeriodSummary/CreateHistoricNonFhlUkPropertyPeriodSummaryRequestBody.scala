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

package v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary

import play.api.libs.functional.syntax.{ toFunctionalBuilderOps, unlift }
import play.api.libs.json.{ JsPath, Json, OWrites, Reads }
import shapeless.HNil
import utils.EmptinessChecker

case class CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(fromDate: String,
                                                                  toDate: String,
                                                                  income: Option[UkNonFhlPropertyIncome],
                                                                  expenses: Option[UkNonFhlPropertyExpenses])

object CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody {

  implicit val emptinessChecker: EmptinessChecker[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody] = EmptinessChecker.use { body =>
    "income"     -> body.income ::
      "expenses" -> body.expenses :: HNil
  }

  implicit val reads: Reads[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody] =
    Json.reads[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "from").write[String] and
      (JsPath \ "to").write[String] and
      (JsPath \ "financials" \ "incomes").writeNullable[UkNonFhlPropertyIncome] and
      (JsPath \ "financials" \ "deductions").writeNullable[UkNonFhlPropertyExpenses]
  )(unlift(CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody.unapply))
}
