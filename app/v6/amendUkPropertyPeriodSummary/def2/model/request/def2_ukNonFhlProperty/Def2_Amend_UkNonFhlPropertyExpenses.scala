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

package v6.amendUkPropertyPeriodSummary.def2.model.request.def2_ukNonFhlProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v6.amendUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom.Def2_Amend_UkPropertyExpensesRentARoom

case class Def2_Amend_UkNonFhlPropertyExpensesSubmission(premisesRunningCosts: Option[BigDecimal],
                                                         repairsAndMaintenance: Option[BigDecimal],
                                                         financialCosts: Option[BigDecimal],
                                                         professionalFees: Option[BigDecimal],
                                                         costOfServices: Option[BigDecimal],
                                                         other: Option[BigDecimal],
                                                         residentialFinancialCost: Option[BigDecimal],
                                                         residentialFinancialCostAmount: Option[BigDecimal],
                                                         travelCosts: Option[BigDecimal],
                                                         residentialFinancialCostsCarriedForward: Option[BigDecimal],
                                                         broughtFwdResidentialFinancialCostAmount: Option[BigDecimal],
                                                         rentARoom: Option[Def2_Amend_UkPropertyExpensesRentARoom],
                                                         consolidatedExpenses: Option[BigDecimal]) {}

object Def2_Amend_UkNonFhlPropertyExpensesSubmission {

  implicit val reads: Reads[Def2_Amend_UkNonFhlPropertyExpensesSubmission] = Json.reads[Def2_Amend_UkNonFhlPropertyExpensesSubmission]

  implicit val writes: Writes[Def2_Amend_UkNonFhlPropertyExpensesSubmission] = (
    (JsPath \ "premisesRunningCosts").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").writeNullable[BigDecimal] and
      (JsPath \ "financialCosts").writeNullable[BigDecimal] and
      (JsPath \ "professionalFees").writeNullable[BigDecimal] and
      (JsPath \ "costOfServices").writeNullable[BigDecimal] and
      (JsPath \ "other").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCost").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostAmount").writeNullable[BigDecimal] and
      (JsPath \ "travelCosts").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostsCarriedForward").writeNullable[BigDecimal] and
      (JsPath \ "broughtFwdResidentialFinancialCostAmount").writeNullable[BigDecimal] and
      (JsPath \ "ukOtherRentARoom").writeNullable[Def2_Amend_UkPropertyExpensesRentARoom] and
      (JsPath \ "consolidatedExpense").writeNullable[BigDecimal]
  )(unlift(Def2_Amend_UkNonFhlPropertyExpensesSubmission.unapply))

}

case class Def2_Amend_UkNonFhlPropertyExpenses(premisesRunningCosts: Option[BigDecimal],
                                               repairsAndMaintenance: Option[BigDecimal],
                                               financialCosts: Option[BigDecimal],
                                               professionalFees: Option[BigDecimal],
                                               costOfServices: Option[BigDecimal],
                                               other: Option[BigDecimal],
                                               residentialFinancialCost: Option[BigDecimal],
                                               travelCosts: Option[BigDecimal],
                                               residentialFinancialCostsCarriedForward: Option[BigDecimal],
                                               rentARoom: Option[Def2_Amend_UkPropertyExpensesRentARoom],
                                               consolidatedExpenses: Option[BigDecimal]) {

  def toSubmissionModel: Def2_Amend_UkNonFhlPropertyExpensesSubmission = {
    Def2_Amend_UkNonFhlPropertyExpensesSubmission(
      premisesRunningCosts = premisesRunningCosts,
      repairsAndMaintenance = repairsAndMaintenance,
      financialCosts = financialCosts,
      professionalFees = professionalFees,
      costOfServices = costOfServices,
      other = other,
      residentialFinancialCost = if (consolidatedExpenses.isDefined) None else residentialFinancialCost,
      residentialFinancialCostAmount = if (consolidatedExpenses.isDefined) residentialFinancialCost else None,
      travelCosts = travelCosts,
      residentialFinancialCostsCarriedForward = if (consolidatedExpenses.isDefined) None else residentialFinancialCostsCarriedForward,
      broughtFwdResidentialFinancialCostAmount = if (consolidatedExpenses.isDefined) residentialFinancialCostsCarriedForward else None,
      rentARoom = rentARoom,
      consolidatedExpenses = consolidatedExpenses
    )
  }

}

object Def2_Amend_UkNonFhlPropertyExpenses {
  implicit val reads: Reads[Def2_Amend_UkNonFhlPropertyExpenses] = Json.reads[Def2_Amend_UkNonFhlPropertyExpenses]

  implicit val writes: Writes[Def2_Amend_UkNonFhlPropertyExpenses] = (
    (JsPath \ "premisesRunningCosts").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").writeNullable[BigDecimal] and
      (JsPath \ "financialCosts").writeNullable[BigDecimal] and
      (JsPath \ "professionalFees").writeNullable[BigDecimal] and
      (JsPath \ "costOfServices").writeNullable[BigDecimal] and
      (JsPath \ "other").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCost").writeNullable[BigDecimal] and
      (JsPath \ "travelCosts").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostsCarriedForward").writeNullable[BigDecimal] and
      (JsPath \ "ukOtherRentARoom").writeNullable[Def2_Amend_UkPropertyExpensesRentARoom] and
      (JsPath \ "consolidatedExpense").writeNullable[BigDecimal]
  )(unlift(Def2_Amend_UkNonFhlPropertyExpenses.unapply))

}
