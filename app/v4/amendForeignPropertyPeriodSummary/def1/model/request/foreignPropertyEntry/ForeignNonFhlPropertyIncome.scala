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

package v4.amendForeignPropertyPeriodSummary.def1.model.request.foreignPropertyEntry

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class ForeignNonFhlPropertyIncome(
    rentIncome: Option[ForeignNonFhlPropertyRentIncome],
    foreignTaxCreditRelief: Boolean,
    premiumsOfLeaseGrant: Option[BigDecimal],
    otherPropertyIncome: Option[BigDecimal],
    foreignTaxPaidOrDeducted: Option[BigDecimal],
    specialWithholdingTaxOrUkTaxPaid: Option[BigDecimal]
)

object ForeignNonFhlPropertyIncome {
  implicit val reads: Reads[ForeignNonFhlPropertyIncome] = Json.reads[ForeignNonFhlPropertyIncome]

  implicit val writes: Writes[ForeignNonFhlPropertyIncome] = (
    (JsPath \ "rentIncome").writeNullable[ForeignNonFhlPropertyRentIncome] and
      (JsPath \ "foreignTaxCreditRelief").write[Boolean] and
      (JsPath \ "premiumsOfLeaseGrant").writeNullable[BigDecimal] and
      (JsPath \ "otherPropertyIncome").writeNullable[BigDecimal] and
      (JsPath \ "foreignTaxPaidOrDeducted").writeNullable[BigDecimal] and
      (JsPath \ "specialWithholdingTaxOrUkTaxPaid").writeNullable[BigDecimal]
  )(unlift(ForeignNonFhlPropertyIncome.unapply))

}
