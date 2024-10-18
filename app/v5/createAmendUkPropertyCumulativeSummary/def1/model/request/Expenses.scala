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

package v5.createAmendUkPropertyCumulativeSummary.def1.model.request

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, Writes}

case class Expenses(premisesRunningCosts: Option[BigDecimal],
                    repairsAndMaintenance: Option[BigDecimal],
                    financialCosts: Option[BigDecimal],
                    professionalFees: Option[BigDecimal],
                    costOfServices: Option[BigDecimal],
                    other: Option[BigDecimal],
                    residentialFinancialCost: Option[BigDecimal],
                    travelCosts: Option[BigDecimal],
                    residentialFinancialCostsCarriedForward: Option[BigDecimal],
                    rentARoom: Option[RentARoomExpenses],
                    consolidatedExpenses: Option[BigDecimal])

object Expenses {

  implicit val reads: Reads[Expenses] = (
    (JsPath \ "premisesRunningCosts").readNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").readNullable[BigDecimal] and
      (JsPath \ "financialCosts").readNullable[BigDecimal] and
      (JsPath \ "professionalFees").readNullable[BigDecimal] and
      (JsPath \ "costOfServices").readNullable[BigDecimal] and
      (JsPath \ "other").readNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCost").readNullable[BigDecimal] and
      (JsPath \ "travelCosts").readNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostsCarriedForward").readNullable[BigDecimal] and
      (JsPath \ "rentARoom").readNullable[RentARoomExpenses] and
      (JsPath \ "consolidatedExpenses").readNullable[BigDecimal]
  )(Expenses.apply _)

  implicit val writes: Writes[Expenses] = (
    (JsPath \ "premisesRunningCosts").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").writeNullable[BigDecimal] and
      (JsPath \ "financialCosts").writeNullable[BigDecimal] and
      (JsPath \ "professionalFees").writeNullable[BigDecimal] and
      (JsPath \ "costOfServices").writeNullable[BigDecimal] and
      (JsPath \ "other").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCost").writeNullable[BigDecimal] and
      (JsPath \ "travelCosts").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostsCarriedForward").writeNullable[BigDecimal] and
      (JsPath \ "ukOtherRentARoom").writeNullable[RentARoomExpenses] and
      (JsPath \ "consolidatedExpenses").writeNullable[BigDecimal]
  )(unlift(Expenses.unapply))

}
