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

package v2.models.request.createUkPropertyPeriodSummary.ukFhlProperty

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._
import v2.models.request.common.ukPropertyRentARoom.UkPropertyExpensesRentARoom

case class UkFhlPropertyExpenses(premisesRunningCosts: Option[BigDecimal],
                                 repairsAndMaintenance: Option[BigDecimal],
                                 financialCosts: Option[BigDecimal],
                                 professionalFees: Option[BigDecimal],
                                 costOfServices: Option[BigDecimal],
                                 other: Option[BigDecimal],
                                 consolidatedExpense: Option[BigDecimal],
                                 travelCosts: Option[BigDecimal],
                                 rentARoom: Option[UkPropertyExpensesRentARoom])


object UkFhlPropertyExpenses {
  implicit val reads: Reads[UkFhlPropertyExpenses] = Json.reads[UkFhlPropertyExpenses]

  implicit val writes: Writes[UkFhlPropertyExpenses] = (
    (JsPath \ "premisesRunningCosts").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").writeNullable[BigDecimal] and
      (JsPath \ "financialCosts").writeNullable[BigDecimal] and
      (JsPath \ "professionalFees").writeNullable[BigDecimal] and
      (JsPath \ "costOfServices").writeNullable[BigDecimal] and
      (JsPath \ "other").writeNullable[BigDecimal] and
      (JsPath \ "consolidatedExpenses").writeNullable[BigDecimal] and
      (JsPath \ "travelCosts").writeNullable[BigDecimal] and
      (JsPath \ "ukFhlRentARoom").writeNullable[UkPropertyExpensesRentARoom]
  ) (unlift(UkFhlPropertyExpenses.unapply))
}