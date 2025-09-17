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

import play.api.libs.json.*

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

  implicit val reads: Reads[Expenses] = Json.reads[Expenses]

  implicit val expensesWrites: Writes[Expenses] = (expenses: Expenses) => {
    val baseJson = Json.obj(
      "premisesRunningCosts"  -> expenses.premisesRunningCosts,
      "repairsAndMaintenance" -> expenses.repairsAndMaintenance,
      "financialCosts"        -> expenses.financialCosts,
      "professionalFees"      -> expenses.professionalFees,
      "costOfServices"        -> expenses.costOfServices,
      "other"                 -> expenses.other,
      "travelCosts"           -> expenses.travelCosts,
      "ukOtherRentARoom"      -> expenses.rentARoom,
      "consolidatedExpenses"  -> expenses.consolidatedExpenses
    )

    val consolidatedNameChangeFields = expenses.consolidatedExpenses match {
      case Some(_) =>
        Json.obj(
          "residentialFinancialCostAmount"           -> expenses.residentialFinancialCost,
          "broughtFwdResidentialFinancialCostAmount" -> expenses.residentialFinancialCostsCarriedForward
        )
      case _ =>
        Json.obj(
          "residentialFinancialCost"                -> expenses.residentialFinancialCost,
          "residentialFinancialCostsCarriedForward" -> expenses.residentialFinancialCostsCarriedForward
        )
    }

    JsObject((baseJson ++ consolidatedNameChangeFields).fields.filter {
      case (_, JsNull) => false
      case _           => true
    })
  }

}
