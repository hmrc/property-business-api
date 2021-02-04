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

package v1.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

case class ForeignFhlEeaExpenditure(premisesRunningCosts: Option[BigDecimal],
                                    repairsAndMaintenance: Option[BigDecimal],
                                    financialCosts: Option[BigDecimal],
                                    professionalFees: Option[BigDecimal],
                                    costsOfServices: Option[BigDecimal],
                                    travelCosts: Option[BigDecimal],
                                    other: Option[BigDecimal],
                                    consolidatedExpenses: Option[BigDecimal])

object ForeignFhlEeaExpenditure {
  implicit val writes: Writes[ForeignFhlEeaExpenditure] = Json.writes[ForeignFhlEeaExpenditure]
  implicit val reads: Reads[ForeignFhlEeaExpenditure] = (
    (JsPath \ "premisesRunningCostsAmount").readNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenanceAmount").readNullable[BigDecimal] and
      (JsPath \ "financialCostsAmount").readNullable[BigDecimal] and
      (JsPath \ "professionalFeesAmount").readNullable[BigDecimal] and
      (JsPath \ "costOfServicesAmount").readNullable[BigDecimal] and
      (JsPath \ "travelCostsAmount").readNullable[BigDecimal] and
      (JsPath \ "otherAmount").readNullable[BigDecimal] and
      (JsPath \ "consolidatedExpensesAmount").readNullable[BigDecimal]
    )(ForeignFhlEeaExpenditure.apply _)
}