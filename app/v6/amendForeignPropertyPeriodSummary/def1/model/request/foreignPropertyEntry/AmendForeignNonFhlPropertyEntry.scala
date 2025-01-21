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

package v6.amendForeignPropertyPeriodSummary.def1.model.request.foreignPropertyEntry

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import shapeless.HNil
import shared.utils.EmptinessChecker

case class AmendForeignNonFhlPropertyEntry(
    countryCode: String,
    income: Option[ForeignNonFhlPropertyIncome],
    expenses: Option[AmendForeignNonFhlPropertyExpenses]
)

object AmendForeignNonFhlPropertyEntry {

  implicit val emptinessChecker: EmptinessChecker[AmendForeignNonFhlPropertyEntry] = EmptinessChecker.use { body =>
    "income"     -> body.income ::
      "expenses" -> body.expenses :: HNil
  }

  implicit val reads: Reads[AmendForeignNonFhlPropertyEntry] = Json.reads[AmendForeignNonFhlPropertyEntry]

  implicit val writes: Writes[AmendForeignNonFhlPropertyEntry] = (
    (JsPath \ "countryCode").write[String] and
      (JsPath \ "income").writeNullable[ForeignNonFhlPropertyIncome] and
      (JsPath \ "expenses").writeNullable[AmendForeignNonFhlPropertyExpenses]
  )(unlift(AmendForeignNonFhlPropertyEntry.unapply))

}
