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

package v5.createForeignPropertyPeriodSummary.def1.model.request.Def1_foreignPropertyEntry

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class Def1_Create_ForeignNonFhlPropertyIncome(
    rentIncome: Option[Def1_Create_ForeignNonFhlPropertyRentIncome],
    foreignTaxCreditRelief: Boolean,
    premiumsOfLeaseGrant: Option[BigDecimal],
    otherPropertyIncome: Option[BigDecimal],
    foreignTaxPaidOrDeducted: Option[BigDecimal],
    specialWithholdingTaxOrUkTaxPaid: Option[BigDecimal]
)

object Def1_Create_ForeignNonFhlPropertyIncome {
  implicit val reads: Reads[Def1_Create_ForeignNonFhlPropertyIncome] = Json.reads[Def1_Create_ForeignNonFhlPropertyIncome]

  implicit val writes: Writes[Def1_Create_ForeignNonFhlPropertyIncome] = (
    (JsPath \ "rentIncome").writeNullable[Def1_Create_ForeignNonFhlPropertyRentIncome] and
      (JsPath \ "foreignTaxCreditRelief").write[Boolean] and
      (JsPath \ "premiumsOfLeaseGrant").writeNullable[BigDecimal] and
      (JsPath \ "otherPropertyIncome").writeNullable[BigDecimal] and
      (JsPath \ "foreignTaxPaidOrDeducted").writeNullable[BigDecimal] and
      (JsPath \ "specialWithholdingTaxOrUkTaxPaid").writeNullable[BigDecimal]
  )(unlift(Def1_Create_ForeignNonFhlPropertyIncome.unapply))

}
