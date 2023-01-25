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

package v2.models.request.amendHistoricFhlUkPiePeriodSummary

import play.api.libs.functional.syntax.{ toFunctionalBuilderOps, unlift }
import play.api.libs.json.{ JsPath, Json, OWrites, Reads }
import v2.models.request.common.ukFhlPieProperty.{ UkFhlPieExpenses, UkFhlPieIncome }

case class AmendHistoricFhlUkPiePeriodSummaryRequestBody(income: Option[UkFhlPieIncome], expenses: Option[UkFhlPieExpenses])

object AmendHistoricFhlUkPiePeriodSummaryRequestBody {

  implicit val reads: Reads[AmendHistoricFhlUkPiePeriodSummaryRequestBody] = Json.reads[AmendHistoricFhlUkPiePeriodSummaryRequestBody]

  implicit val writes: OWrites[AmendHistoricFhlUkPiePeriodSummaryRequestBody] = (
    (JsPath \ "incomes").writeNullable[UkFhlPieIncome] and
      (JsPath \ "deductions").writeNullable[UkFhlPieExpenses]
  )(unlift(AmendHistoricFhlUkPiePeriodSummaryRequestBody.unapply))
}
