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

package v5.retrieveForeignPropertyCumulativeSummary.def1.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Income(rentIncome: Option[RentIncome],
                  foreignTaxCreditRelief: Option[Boolean],
                  premiumsOfLeaseGrant: Option[BigDecimal],
                  otherPropertyIncome: Option[BigDecimal],
                  foreignTaxPaidOrDeducted: Option[BigDecimal],
                  specialWithholdingTaxOrUkTaxPaid: Option[BigDecimal])

object Income {
  implicit val writes: OWrites[Income] = Json.writes[Income]

  implicit val reads: Reads[Income] = (
    (JsPath \ "rentIncome").readNullable[RentIncome] and
      (JsPath \ "foreignTaxCreditRelief").readNullable[Boolean] and
      (JsPath \ "premiumsOfLeaseGrantAmount").readNullable[BigDecimal] and
      (JsPath \ "otherPropertyIncomeAmount").readNullable[BigDecimal] and
      (JsPath \ "foreignTaxPaidOrDeducted").readNullable[BigDecimal] and
      (JsPath \ "specialWithholdingTaxOrUkTaxPaid").readNullable[BigDecimal]
  )(Income.apply _)

}
