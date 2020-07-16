/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.foreignPropertyPeriodSummary.retrieveForeignPropertyPeriodSummary.foreignProperty

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

case class ForeignPropertyIncome(rentIncome: ForeignPropertyRentIncome,
                                 foreignTaxCreditRelief: Boolean,
                                 premiumOfLeaseGrant: Option[BigDecimal],
                                 otherPropertyIncome: Option[BigDecimal],
                                 foreignTaxTakenOff: Option[BigDecimal],
                                 specialWithholdingTaxOrUKTaxPaid: Option[BigDecimal])

object ForeignPropertyIncome {
  implicit val reads: Writes[ForeignPropertyIncome] = Json.writes[ForeignPropertyIncome]
  implicit val writes: Reads[ForeignPropertyIncome] = (
    (JsPath \ "rentIncome").read[ForeignPropertyRentIncome] and
      (JsPath \ "foreignTaxCreditRelief").read[Boolean] and
      (JsPath \ "premiumOfLeaseGrantAmount").readNullable[BigDecimal] and
      (JsPath \ "otherPropertyIncomeAmount").readNullable[BigDecimal] and
      (JsPath \ "foreignTaxPaidOrDeducted").readNullable[BigDecimal] and
      (JsPath \ "specialWithholdingTaxOrUKTaxPaid").readNullable[BigDecimal]
    )(ForeignPropertyIncome.apply _)
}
