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

package v4.retrieveHistoricFhlUkPropertyPeriodSummary.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v4.retrieveHistoricFhlUkPropertyPeriodSummary.def1.model.response._

sealed trait RetrieveHistoricFhlUkPiePeriodSummaryResponse

object RetrieveHistoricFhlUkPiePeriodSummaryResponse {

  implicit val writes: OWrites[RetrieveHistoricFhlUkPiePeriodSummaryResponse] = {
    case def1: Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse => Json.toJsObject(def1)
  }

}

case class Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse(fromDate: String,
                                                         toDate: String,
                                                         income: Option[PeriodIncome],
                                                         expenses: Option[PeriodExpenses])
  extends RetrieveHistoricFhlUkPiePeriodSummaryResponse

object Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse {
  implicit val writes: OWrites[Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse] = Json.writes[Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse]

  implicit val reads: Reads[Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse] = (
    (JsPath \ "from").read[String] and
      (JsPath \ "to").read[String] and
      (JsPath \ "financials" \ "incomes").readNullable[PeriodIncome] and
      (JsPath \ "financials" \ "deductions").readNullable[PeriodExpenses]
    )(Def1_RetrieveHistoricFhlUkPiePeriodSummaryResponse.apply _)
}
