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

package v4.retrieveHistoricNonFhlUkPropertyPeriodSummary.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v4.retrieveHistoricNonFhlUkPropertyPeriodSummary.def1.model.response.{PeriodExpenses, PeriodIncome}

sealed trait RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse

object RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse {

  implicit val writes: OWrites[RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse] = {
    case def1: Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse =>
      Json.toJsObject(def1)
  }

}

case class Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse(
    fromDate: String,
    toDate: String,
    income: Option[PeriodIncome],
    expenses: Option[PeriodExpenses]
) extends RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse

object Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse {

  implicit val writes: OWrites[Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse] =
    Json.writes[Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse]

  implicit val reads: Reads[Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse] = (
    (JsPath \ "from").read[String] and
      (JsPath \ "to").read[String] and
      (JsPath \ "financials" \ "incomes").readNullable[PeriodIncome] and
      (JsPath \ "financials" \ "deductions").readNullable[PeriodExpenses]
  )(Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse.apply _)

}
