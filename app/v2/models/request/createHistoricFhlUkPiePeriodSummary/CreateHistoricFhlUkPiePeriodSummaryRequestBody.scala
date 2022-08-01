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

package v2.models.request.createHistoricFhlUkPiePeriodSummary

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, OWrites, Reads, __}
import v2.models.request.common.ukFhlPieProperty.{UkFhlPiePropertyExpenses, UkFhlPiePropertyIncome}

case class CreateHistoricFhlUkPiePeriodSummaryRequestBody (fromDate: String,
                                                           toDate: String,
                                                           income: Option[UkFhlPiePropertyIncome],
                                                           expenses: Option[UkFhlPiePropertyExpenses])
object CreateHistoricFhlUkPiePeriodSummaryRequestBody {

  implicit val reads: Reads[CreateHistoricFhlUkPiePeriodSummaryRequestBody] =  (
      (__ \ "fromDate").read[String] and
      (__ \ "toDate" ).read[String] and
      (__ \ "income").readNullable[UkFhlPiePropertyIncome] and
      (__ \ "expenses").readNullable[UkFhlPiePropertyExpenses]
    )(CreateHistoricFhlUkPiePeriodSummaryRequestBody.apply _)

  implicit val writes: OWrites[CreateHistoricFhlUkPiePeriodSummaryRequestBody] = (
      (JsPath \ "from").write[String] and
      (JsPath \ "to").write[String] and
      (JsPath \ "financials" \ "incomes").writeNullable[UkFhlPiePropertyIncome] and
      (JsPath \ "financials" \ "deductions").writeNullable[UkFhlPiePropertyExpenses]
    )(unlift(CreateHistoricFhlUkPiePeriodSummaryRequestBody.unapply))
}
