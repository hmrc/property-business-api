/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.models.request.common.foreignPropertyEntry

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class CreateForeignNonFhlPropertyEntry(
                                 countryCode: String,
                                 income: ForeignNonFhlPropertyIncome,
                                 expenses: Option[CreateForeignNonFhlPropertyExpenses]
                               )

object CreateForeignNonFhlPropertyEntry {
  implicit val reads: Reads[CreateForeignNonFhlPropertyEntry] = Json.reads[CreateForeignNonFhlPropertyEntry]

  implicit val writes: Writes[CreateForeignNonFhlPropertyEntry] = (
    (JsPath \ "countryCode").write[String] and
      (JsPath \ "income").write[ForeignNonFhlPropertyIncome] and
      (JsPath \ "expenses").writeNullable[CreateForeignNonFhlPropertyExpenses]
    ) (unlift(CreateForeignNonFhlPropertyEntry.unapply))
}