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

package v4.amendHistoricNonFhlUkPropertyPeriodSummary.model.request

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import v4.amendHistoricNonFhlUkPropertyPeriodSummary.def1.model.request.{UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}

sealed trait AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody

case class Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
    income: Option[UkNonFhlPropertyIncome],
    expenses: Option[UkNonFhlPropertyExpenses]
) extends AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody

object Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody {

  implicit val reads: Reads[Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody] =
    Json.reads[Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody]

  implicit val writes: OWrites[Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody] = (
    (JsPath \ "incomes").writeNullable[UkNonFhlPropertyIncome] and
      (JsPath \ "deductions").writeNullable[UkNonFhlPropertyExpenses]
  )(unlift(Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody.unapply))

  implicit val format: OFormat[Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryRequestBody] = OFormat(reads, writes)
}
