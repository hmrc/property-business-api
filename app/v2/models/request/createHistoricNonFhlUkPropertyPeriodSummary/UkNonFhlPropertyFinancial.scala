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

package v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary

import play.api.libs.functional.syntax.{ toFunctionalBuilderOps, unlift }
import play.api.libs.json.{ JsPath, Json, Reads, Writes }

case class RentIncome(amount: Option[BigDecimal], taxDeducted: Option[BigDecimal])

object RentIncome {
  implicit val reads: Reads[RentIncome] = Json.reads[RentIncome]

  implicit val writes: Writes[RentIncome] = (
    (JsPath \ "amount").writeNullable[BigDecimal] and
      (JsPath \ "taxDeducted").writeNullable[BigDecimal]
  )(unlift(RentIncome.unapply))
}

case class RentARoomIncomes(rentsReceived: BigDecimal)

object RentARoomIncomes {
  implicit val reads: Reads[RentARoomIncomes] = Json.reads[RentARoomIncomes]

  implicit val writes: Writes[RentARoomIncomes] = Json.writes[RentARoomIncomes]
}

case class Incomes(rentIncome: Option[RentIncome],
                   premiumsOfLeaseGrant: Option[BigDecimal],
                   reversePremiums: Option[BigDecimal],
                   otherIncome: Option[BigDecimal],
                   ukRentARoom: Option[RentARoomIncomes])

object Incomes {
  implicit val reads: Reads[Incomes] = Json.reads[Incomes]

  implicit val writes: Writes[Incomes] = (
    (JsPath \ "rentIncome").writeNullable[RentIncome] and
      (JsPath \ "premiumsOfLeaseGrant").writeNullable[BigDecimal] and
      (JsPath \ "reversePremiums").writeNullable[BigDecimal] and
      (JsPath \ "otherIncome").writeNullable[BigDecimal] and
      (JsPath \ "ukRentARoom").writeNullable[RentARoomIncomes]
  )(unlift(Incomes.unapply))
}

case class RentARoomDeductions(amountClaimed: BigDecimal)

object RentARoomDeductions {
  implicit val reads: Reads[RentARoomDeductions] = Json.reads[RentARoomDeductions]

  implicit val writes: Writes[RentARoomDeductions] = Json.writes[RentARoomDeductions]
}

case class Deductions(premisesRunningCosts: Option[BigDecimal],
                      repairsAndMaintenance: Option[BigDecimal],
                      financialCosts: Option[BigDecimal],
                      professionalFees: Option[BigDecimal],
                      costOfServices: Option[BigDecimal],
                      other: Option[BigDecimal],
                      travelCosts: Option[BigDecimal],
                      residentialFinancialCostsCarriedForward: Option[BigDecimal],
                      residentialFinancialCost: Option[BigDecimal],
                      ukRentARoom: Option[RentARoomDeductions])

object Deductions {
  implicit val reads: Reads[Deductions] = Json.reads[Deductions]

  implicit val writes: Writes[Deductions] = (
    (JsPath \ "premisesRunningCosts").writeNullable[BigDecimal] and
      (JsPath \ "repairsAndMaintenance").writeNullable[BigDecimal] and
      (JsPath \ "financialCosts").writeNullable[BigDecimal] and
      (JsPath \ "professionalFees").writeNullable[BigDecimal] and
      (JsPath \ "costOfServices").writeNullable[BigDecimal] and
      (JsPath \ "other").writeNullable[BigDecimal] and
      (JsPath \ "travelCosts").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCostsCarriedForward").writeNullable[BigDecimal] and
      (JsPath \ "residentialFinancialCost").writeNullable[BigDecimal] and
      (JsPath \ "ukRentARoom").writeNullable[RentARoomDeductions]
  )(unlift(Deductions.unapply))
}

case class UkNonFhlPropertyFinancial(incomes: Option[Incomes], deductions: Option[Deductions])

object UkNonFhlPropertyFinancial {
  implicit val reads: Reads[UkNonFhlPropertyFinancial] = Json.reads[UkNonFhlPropertyFinancial]

  implicit val writes: Writes[UkNonFhlPropertyFinancial] = Json.writes[UkNonFhlPropertyFinancial]
}
