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

package v4.createForeignPropertyPeriodSummary.def1.model.request.Def1_foreignPropertyEntry

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import shared.utils.EmptinessChecker
import shared.utils.EmptinessChecker.field

case class Def1_Create_CreateForeignNonFhlPropertyEntry(
    countryCode: String,
    income: Option[Def1_Create_ForeignNonFhlPropertyIncome],
    expenses: Option[Def1_Create_CreateForeignNonFhlPropertyExpenses]
)

object Def1_Create_CreateForeignNonFhlPropertyEntry {

  implicit val emptinessChecker: EmptinessChecker[Def1_Create_CreateForeignNonFhlPropertyEntry] = EmptinessChecker.use { body =>
    List(
      field("income", body.income),
      field("expenses", body.expenses)
    )
  }

  implicit val reads: Reads[Def1_Create_CreateForeignNonFhlPropertyEntry] = Json.reads[Def1_Create_CreateForeignNonFhlPropertyEntry]

  implicit val writes: Writes[Def1_Create_CreateForeignNonFhlPropertyEntry] = (
    (JsPath \ "countryCode").write[String] and
      (JsPath \ "income").writeNullable[Def1_Create_ForeignNonFhlPropertyIncome] and
      (JsPath \ "expenses").writeNullable[Def1_Create_CreateForeignNonFhlPropertyExpenses]
  )(o => Tuple.fromProductTyped(o))

}
