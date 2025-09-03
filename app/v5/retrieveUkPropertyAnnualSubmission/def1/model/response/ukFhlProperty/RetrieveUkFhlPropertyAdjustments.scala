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

package v5.retrieveUkPropertyAnnualSubmission.def1.model.response.ukFhlProperty

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class RetrieveUkFhlPropertyAdjustments(
    privateUseAdjustment: Option[BigDecimal],
    balancingCharge: Option[BigDecimal],
    periodOfGraceAdjustment: Boolean,
    businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
    nonResidentLandlord: Boolean,
    rentARoom: Option[RetrieveUkFhlPropertyRentARoom]
)

object RetrieveUkFhlPropertyAdjustments {
  implicit val writes: OWrites[RetrieveUkFhlPropertyAdjustments] = Json.writes[RetrieveUkFhlPropertyAdjustments]

  implicit val reads: Reads[RetrieveUkFhlPropertyAdjustments] = (
    (__ \ "privateUseAdjustment").readNullable[BigDecimal] and
      (__ \ "balancingCharge").readNullable[BigDecimal] and
      (__ \ "periodOfGraceAdjustment").read[Boolean] and
      (__ \ "businessPremisesRenovationAllowanceBalancingCharges").readNullable[BigDecimal] and
      (__ \ "nonResidentLandlord").read[Boolean] and
      (__ \ "ukFhlRentARoom").readNullable[RetrieveUkFhlPropertyRentARoom]
  )(RetrieveUkFhlPropertyAdjustments.apply _)

}
