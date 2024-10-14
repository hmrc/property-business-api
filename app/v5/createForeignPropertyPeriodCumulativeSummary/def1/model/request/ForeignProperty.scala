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

package v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import shapeless.HNil
import utils.EmptinessChecker

case class ForeignProperty(
    countryCode: String,
    income: Option[PropertyIncome],
    expenses: Option[Expenses]
)

object ForeignProperty {

  implicit val emptinessChecker: EmptinessChecker[ForeignProperty] = EmptinessChecker.use { body =>
    "income"     -> body.income ::
      "expenses" -> body.expenses :: HNil
  }

  implicit val reads: Reads[ForeignProperty] = Json.reads[ForeignProperty]

  implicit val writes: Writes[ForeignProperty] = (
    (JsPath \ "countryCode").write[String] and
      (JsPath \ "income").writeNullable[PropertyIncome] and
      (JsPath \ "expenses").writeNullable[Expenses]
  )(unlift(ForeignProperty.unapply))

}
