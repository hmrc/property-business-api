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

package v4.historicFhlUkPropertyPeriodSummary.retrieve.model.response

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v4.historicFhlUkPropertyPeriodSummary.retrieve.def1.model.response.*

sealed trait RetrieveHistoricFhlUkPropertyPeriodSummaryResponse

object RetrieveHistoricFhlUkPropertyPeriodSummaryResponse {

  implicit val writes: OWrites[RetrieveHistoricFhlUkPropertyPeriodSummaryResponse] = {
    case def1: Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse =>
      Json.toJsObject(def1)
  }

}

case class Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse(
    fromDate: String,
    toDate: String,
    income: Option[PeriodIncome],
    expenses: Option[PeriodExpenses]
) extends RetrieveHistoricFhlUkPropertyPeriodSummaryResponse

object Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse {

  implicit val writes: OWrites[Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse] =
    Json.writes[Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse]

  implicit val reads: Reads[Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse] = (
    (JsPath \ "from").read[String] and
      (JsPath \ "to").read[String] and
      (JsPath \ "financials" \ "incomes").readNullable[PeriodIncome] and
      (JsPath \ "financials" \ "deductions").readNullable[PeriodExpenses]
  )(Def1_RetrieveHistoricFhlUkPropertyPeriodSummaryResponse.apply)

}
