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

package v1.models.request.createForeignProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class ForeignPropertyIncome(rentIncome: RentIncome,
                                 foreignTaxCreditRelief: Boolean,
                                 premiumOfLeaseGrant: Option[BigDecimal],
                                 otherPropertyIncome: Option[BigDecimal],
                                 foreignTaxTakenOff: Option[BigDecimal],
                                 specialWithholdingTaxOrUKTaxPaid: Option[BigDecimal]
                                )

object ForeignPropertyIncome {
  implicit val reads: Reads[ForeignPropertyIncome] = Json.reads[ForeignPropertyIncome]
  implicit val writes: Writes[ForeignPropertyIncome] = (
    (JsPath \ "rentIncome").write[RentIncome] and
      (JsPath \ "foreignTaxCreditRelief").write[Boolean] and
      (JsPath \ "premiumOfLeaseGrantAmount").writeNullable[BigDecimal] and
      (JsPath \ "otherPropertyIncomeAmount").writeNullable[BigDecimal] and
      (JsPath \ "foreignTaxTakenOff").writeNullable[BigDecimal] and
      (JsPath \ "specialWithholdingTaxOrUKTaxPaid").writeNullable[BigDecimal]
    )(unlift(ForeignPropertyIncome.unapply))
}
