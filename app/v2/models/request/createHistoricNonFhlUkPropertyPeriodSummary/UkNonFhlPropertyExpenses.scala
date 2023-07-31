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

package v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v2.models.request.common.ukPropertyRentARoom.UkPropertyExpensesRentARoom

case class UkNonFhlPropertyExpenses(premisesRunningCosts: Option[BigDecimal],
                                    repairsAndMaintenance: Option[BigDecimal],
                                    financialCosts: Option[BigDecimal],
                                    professionalFees: Option[BigDecimal],
                                    costOfServices: Option[BigDecimal],
                                    other: Option[BigDecimal],
                                    residentialFinancialCost: Option[BigDecimal],
                                    travelCosts: Option[BigDecimal],
                                    residentialFinancialCostsCarriedForward: Option[BigDecimal],
                                    rentARoom: Option[UkPropertyExpensesRentARoom],
                                    consolidatedExpenses: Option[BigDecimal])

object UkNonFhlPropertyExpenses {
  implicit val reads: Reads[UkNonFhlPropertyExpenses] = Json.reads[UkNonFhlPropertyExpenses]

  implicit val writes: Writes[UkNonFhlPropertyExpenses] = (
    (JsPath \ "premisesRunningCosts").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").writeNullable[BigDecimal] and
      (JsPath \ "financialCosts").writeNullable[BigDecimal] and
      (JsPath \ "professionalFees").writeNullable[BigDecimal] and
      (JsPath \ "costOfServices").writeNullable[BigDecimal] and
      (JsPath \ "other").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCost").writeNullable[BigDecimal] and
      (JsPath \ "travelCosts").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostsCarriedForward").writeNullable[BigDecimal] and
      (JsPath \ "ukRentARoom").writeNullable[UkPropertyExpensesRentARoom] and
      (JsPath \ "consolidatedExpenses").writeNullable[BigDecimal]
  )(unlift(UkNonFhlPropertyExpenses.unapply))

}
