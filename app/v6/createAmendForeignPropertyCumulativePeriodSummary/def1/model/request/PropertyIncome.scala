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

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class PropertyIncome(rentIncome: Option[RentIncome],
                          foreignTaxCreditRelief: Option[Boolean],
                          premiumsOfLeaseGrant: Option[BigDecimal],
                          otherPropertyIncome: Option[BigDecimal],
                          foreignTaxPaidOrDeducted: Option[BigDecimal],
                          specialWithholdingTaxOrUkTaxPaid: Option[BigDecimal])

object PropertyIncome {
  implicit val reads: Reads[PropertyIncome] = Json.reads[PropertyIncome]

  implicit val writes: Writes[PropertyIncome] = (
    (JsPath \ "rentIncome").writeNullable[RentIncome] and
      (JsPath \ "foreignTaxCreditRelief").writeNullable[Boolean] and
      (JsPath \ "premiumsOfLeaseGrantAmount").writeNullable[BigDecimal] and
      (JsPath \ "otherPropertyIncomeAmount").writeNullable[BigDecimal] and
      (JsPath \ "foreignTaxPaidOrDeducted").writeNullable[BigDecimal] and
      (JsPath \ "specialWithholdingTaxOrUkTaxPaid").writeNullable[BigDecimal]
  )(unlift(PropertyIncome.unapply))

}
