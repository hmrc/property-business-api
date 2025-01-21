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

package v5.amendUkPropertyPeriodSummary.def1.model.request.def1_ukFhlProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v5.amendUkPropertyPeriodSummary.def1.model.request.def1_ukPropertyRentARoom.Def1_Amend_UkPropertyExpensesRentARoom

case class Def1_Amend_UkFhlPropertyExpenses(premisesRunningCosts: Option[BigDecimal],
                                            repairsAndMaintenance: Option[BigDecimal],
                                            financialCosts: Option[BigDecimal],
                                            professionalFees: Option[BigDecimal],
                                            costOfServices: Option[BigDecimal],
                                            other: Option[BigDecimal],
                                            consolidatedExpenses: Option[BigDecimal],
                                            travelCosts: Option[BigDecimal],
                                            rentARoom: Option[Def1_Amend_UkPropertyExpensesRentARoom])

object Def1_Amend_UkFhlPropertyExpenses {
  implicit val reads: Reads[Def1_Amend_UkFhlPropertyExpenses] = Json.reads[Def1_Amend_UkFhlPropertyExpenses]

  implicit val writes: Writes[Def1_Amend_UkFhlPropertyExpenses] = (
    (JsPath \ "premisesRunningCosts").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").writeNullable[BigDecimal] and
      (JsPath \ "financialCosts").writeNullable[BigDecimal] and
      (JsPath \ "professionalFees").writeNullable[BigDecimal] and
      (JsPath \ "costOfServices").writeNullable[BigDecimal] and
      (JsPath \ "other").writeNullable[BigDecimal] and
      (JsPath \ "consolidatedExpense").writeNullable[BigDecimal] and
      (JsPath \ "travelCosts").writeNullable[BigDecimal] and
      (JsPath \ "ukFhlRentARoom").writeNullable[Def1_Amend_UkPropertyExpensesRentARoom]
  )(unlift(Def1_Amend_UkFhlPropertyExpenses.unapply))

}
