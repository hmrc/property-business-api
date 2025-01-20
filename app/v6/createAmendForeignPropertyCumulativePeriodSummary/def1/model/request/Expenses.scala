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

package v6.createAmendForeignPropertyCumulativePeriodSummary.def1.model.request

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class Expenses(premisesRunningCosts: Option[BigDecimal],
                    repairsAndMaintenance: Option[BigDecimal],
                    financialCosts: Option[BigDecimal],
                    professionalFees: Option[BigDecimal],
                    costOfServices: Option[BigDecimal],
                    travelCosts: Option[BigDecimal],
                    residentialFinancialCost: Option[BigDecimal],
                    broughtFwdResidentialFinancialCost: Option[BigDecimal],
                    other: Option[BigDecimal],
                    consolidatedExpenses: Option[BigDecimal])

object Expenses {
  implicit val reads: Reads[Expenses] = Json.reads[Expenses]

  implicit val writes: Writes[Expenses] = (
    (JsPath \ "premisesRunningCostsAmount").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenanceAmount").writeNullable[BigDecimal] and
      (JsPath \ "financialCostsAmount").writeNullable[BigDecimal] and
      (JsPath \ "professionalFeesAmount").writeNullable[BigDecimal] and
      (JsPath \ "costOfServicesAmount").writeNullable[BigDecimal] and
      (JsPath \ "travelCostsAmount").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostAmount").writeNullable[BigDecimal] and
      (JsPath \ "broughtFwdResidentialFinancialCostAmount").writeNullable[BigDecimal] and
      (JsPath \ "otherAmount").writeNullable[BigDecimal] and
      (JsPath \ "consolidatedExpenseAmount").writeNullable[BigDecimal]
  )(unlift(Expenses.unapply))

}
