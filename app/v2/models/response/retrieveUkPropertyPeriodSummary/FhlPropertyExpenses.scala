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

package v2.models.response.retrieveUkPropertyPeriodSummary

import play.api.libs.json.{ JsPath, Json, OWrites, Reads }
import play.api.libs.functional.syntax._

case class FhlPropertyExpenses(premisesRunningCosts: Option[BigDecimal],
                               repairsAndMaintenance: Option[BigDecimal],
                               financialCosts: Option[BigDecimal],
                               professionalFees: Option[BigDecimal],
                               costOfServices: Option[BigDecimal],
                               other: Option[BigDecimal],
                               travelCosts: Option[BigDecimal],
                               rentARoom: Option[RentARoomExpenses],
                               consolidatedExpenses: Option[BigDecimal])

object FhlPropertyExpenses {
  implicit val writes: OWrites[FhlPropertyExpenses] = Json.writes[FhlPropertyExpenses]

  implicit val reads: Reads[FhlPropertyExpenses] = (
    (JsPath \ "premisesRunningCosts").readNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").readNullable[BigDecimal] and
      (JsPath \ "financialCosts").readNullable[BigDecimal] and
      (JsPath \ "professionalFees").readNullable[BigDecimal] and
      (JsPath \ "costOfServices").readNullable[BigDecimal] and
      (JsPath \ "other").readNullable[BigDecimal] and
      (JsPath \ "travelCosts").readNullable[BigDecimal] and
      (JsPath \ "ukFhlRentARoom").readNullable[RentARoomExpenses] and
      (JsPath \ "consolidatedExpense").readNullable[BigDecimal]
  )(FhlPropertyExpenses.apply _)
}
