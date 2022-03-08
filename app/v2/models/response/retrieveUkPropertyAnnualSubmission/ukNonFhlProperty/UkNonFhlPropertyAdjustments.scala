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

package v2.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class UkNonFhlPropertyAdjustments(
    balancingCharge: Option[BigDecimal],
    privateUseAdjustment: Option[BigDecimal],
    businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
    nonResidentLandlord: Boolean,
    rentARoom: Option[UkNonFhlPropertyRentARoom]
)

object UkNonFhlPropertyAdjustments {
  implicit val writes: OWrites[UkNonFhlPropertyAdjustments] = Json.writes[UkNonFhlPropertyAdjustments]

  implicit val reads: Reads[UkNonFhlPropertyAdjustments] = (
    (__ \ "balancingCharge").readNullable[BigDecimal] and
      (__ \ "privateUseAdjustment").readNullable[BigDecimal] and
      (__ \ "businessPremisesRenovationAllowanceBalancingCharges").readNullable[BigDecimal] and
      (__ \ "nonResidentLandlord").read[Boolean] and
      (__ \ "ukOtherRentARoom").readNullable[UkNonFhlPropertyRentARoom]
  )(UkNonFhlPropertyAdjustments.apply _)
}
