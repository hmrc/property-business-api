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

package v6.createForeignPropertyPeriodSummary.def2.model.request.Def2_foreignPropertyEntry

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import shapeless.HNil
import shared.utils.EmptinessChecker

case class Def2_Create_CreateForeignNonFhlPropertyEntry(
    countryCode: String,
    income: Option[Def2_Create_ForeignNonFhlPropertyIncome],
    expenses: Option[Def2_Create_CreateForeignNonFhlPropertyExpenses]
)

object Def2_Create_CreateForeignNonFhlPropertyEntry {

  implicit val emptinessChecker: EmptinessChecker[Def2_Create_CreateForeignNonFhlPropertyEntry] = EmptinessChecker.use { body =>
    "income"     -> body.income ::
      "expenses" -> body.expenses :: HNil
  }

  implicit val reads: Reads[Def2_Create_CreateForeignNonFhlPropertyEntry] = Json.reads[Def2_Create_CreateForeignNonFhlPropertyEntry]

  implicit val writes: Writes[Def2_Create_CreateForeignNonFhlPropertyEntry] = (
    (JsPath \ "countryCode").write[String] and
      (JsPath \ "income").writeNullable[Def2_Create_ForeignNonFhlPropertyIncome] and
      (JsPath \ "expenses").writeNullable[Def2_Create_CreateForeignNonFhlPropertyExpenses]
  )(unlift(Def2_Create_CreateForeignNonFhlPropertyEntry.unapply))

}
