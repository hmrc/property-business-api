/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.models.request.common.foreignFhlEea

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class CreateForeignFhlEeaExpenses(
                        premisesRunningCosts: Option[BigDecimal],
                        repairsAndMaintenance: Option[BigDecimal],
                        financialCosts: Option[BigDecimal],
                        professionalFees: Option[BigDecimal],
                        costOfServices: Option[BigDecimal],
                        travelCosts: Option[BigDecimal],
                        other: Option[BigDecimal],
                        consolidatedExpenses: Option[BigDecimal])

object CreateForeignFhlEeaExpenses {
  implicit val reads: Reads[CreateForeignFhlEeaExpenses] = Json.reads[CreateForeignFhlEeaExpenses]

  implicit val writes: Writes[CreateForeignFhlEeaExpenses] = (
    (JsPath \ "premisesRunningCosts").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").writeNullable[BigDecimal] and
      (JsPath \ "financialCosts").writeNullable[BigDecimal] and
      (JsPath \ "professionalFees").writeNullable[BigDecimal] and
      (JsPath \ "costOfServices").writeNullable[BigDecimal] and
      (JsPath \ "travelCosts").writeNullable[BigDecimal] and
      (JsPath \ "other").writeNullable[BigDecimal] and
      (JsPath \ "consolidatedExpenseAmount").writeNullable[BigDecimal]
    ) (unlift(CreateForeignFhlEeaExpenses.unapply))
}
