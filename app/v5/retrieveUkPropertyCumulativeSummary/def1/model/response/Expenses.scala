/*
 * Copyright 2025 HM Revenue & Customs
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

package v5.retrieveUkPropertyCumulativeSummary.def1.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json._

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
                    consolidatedExpenses: Option[BigDecimal]) {}

object Expenses {
  implicit val writes: OWrites[Expenses] = Json.writes[Expenses]

  implicit val reads: Reads[Expenses] = (
    (JsPath \ "premisesRunningCosts").readNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").readNullable[BigDecimal] and
      (JsPath \ "financialCosts").readNullable[BigDecimal] and
      (JsPath \ "professionalFees").readNullable[BigDecimal] and
      (JsPath \ "costOfServices").readNullable[BigDecimal] and
      (JsPath \ "other").readNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCost").readNullable[BigDecimal].flatMap {
        case Some(value) => Reads.pure(Some(value): Option[BigDecimal])
        case None        => (JsPath \ "residentialFinancialCostAmount").readNullable[BigDecimal]
      } and
      (JsPath \ "travelCosts").readNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostsCarriedForward").readNullable[BigDecimal].flatMap {
        case Some(value) => Reads.pure(Some(value): Option[BigDecimal])
        case None        => (JsPath \ "broughtFwdResidentialFinancialCostAmount").readNullable[BigDecimal]
      } and
      (JsPath \ "ukOtherRentARoom").readNullable[RentARoomExpenses] and
      (JsPath \ "consolidatedExpenses").readNullable[BigDecimal]
  )(Expenses.apply _)

}
