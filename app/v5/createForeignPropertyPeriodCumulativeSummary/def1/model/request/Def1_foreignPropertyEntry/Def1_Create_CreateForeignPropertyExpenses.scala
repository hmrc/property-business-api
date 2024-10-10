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

package v5.createForeignPropertyPeriodCumulativeSummary.def1.model.request.Def1_foreignPropertyEntry

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class Def1_Create_CreateForeignPropertyExpenses(premisesRunningCosts: Option[BigDecimal],
                                                     repairsAndMaintenance: Option[BigDecimal],
                                                     financialCostsAmount: Option[BigDecimal],
                                                     professionalFeesAmount: Option[BigDecimal],
                                                     costOfServicesAmount: Option[BigDecimal],
                                                     travelCostsAmount: Option[BigDecimal],
                                                     residentialFinancialCostAmount: Option[BigDecimal],
                                                     broughtFwdResidentialFinancialCostAmount: Option[BigDecimal],
                                                     otherAmount: Option[BigDecimal],
                                                     consolidatedExpenseAmount: Option[BigDecimal])

object Def1_Create_CreateForeignPropertyExpenses {
  implicit val reads: Reads[Def1_Create_CreateForeignPropertyExpenses] = Json.reads[Def1_Create_CreateForeignPropertyExpenses]

  implicit val writes: Writes[Def1_Create_CreateForeignPropertyExpenses] = (
    (JsPath \ "premisesRunningCosts").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").writeNullable[BigDecimal] and
      (JsPath \ "financialCostsAmount").writeNullable[BigDecimal] and
      (JsPath \ "professionalFeesAmount").writeNullable[BigDecimal] and
      (JsPath \ "costOfServicesAmount").writeNullable[BigDecimal] and
      (JsPath \ "travelCostsAmount").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostAmount").writeNullable[BigDecimal] and
      (JsPath \ "broughtFwdResidentialFinancialCostAmount").writeNullable[BigDecimal] and
      (JsPath \ "otherAmount").writeNullable[BigDecimal] and
      (JsPath \ "consolidatedExpenseAmount").writeNullable[BigDecimal]
  )(unlift(Def1_Create_CreateForeignPropertyExpenses.unapply))

}
