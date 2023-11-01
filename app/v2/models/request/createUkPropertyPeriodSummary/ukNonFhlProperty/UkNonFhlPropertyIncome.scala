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

package v2.models.request.createUkPropertyPeriodSummary.ukNonFhlProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v2.models.request.common.ukPropertyRentARoom.UkPropertyIncomeRentARoom

case class UkNonFhlPropertyIncome(premiumsOfLeaseGrant: Option[BigDecimal],
                                  reversePremiums: Option[BigDecimal],
                                  periodAmount: Option[BigDecimal],
                                  taxDeducted: Option[BigDecimal],
                                  otherIncome: Option[BigDecimal],
                                  rentARoom: Option[UkPropertyIncomeRentARoom])

object UkNonFhlPropertyIncome {
  implicit val reads: Reads[UkNonFhlPropertyIncome] = Json.reads[UkNonFhlPropertyIncome]

  implicit val writes: Writes[UkNonFhlPropertyIncome] = (
    (JsPath \ "premiumsOfLeaseGrant").writeNullable[BigDecimal] and
      (JsPath \ "reversePremiums").writeNullable[BigDecimal] and
      (JsPath \ "periodAmount").writeNullable[BigDecimal] and
      (JsPath \ "taxDeducted").writeNullable[BigDecimal] and
      (JsPath \ "otherIncome").writeNullable[BigDecimal] and
      (JsPath \ "ukOtherRentARoom").writeNullable[UkPropertyIncomeRentARoom]
  )(unlift(UkNonFhlPropertyIncome.unapply))

}
