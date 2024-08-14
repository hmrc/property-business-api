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

package v5.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukNonFhlProperty

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Def1_Retrieve_UkNonFhlPropertyAdjustments(
    balancingCharge: Option[BigDecimal],
    privateUseAdjustment: Option[BigDecimal],
    businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
    nonResidentLandlord: Boolean,
    rentARoom: Option[Def1_Retrieve_UkNonFhlPropertyRentARoom]
)

object Def1_Retrieve_UkNonFhlPropertyAdjustments {
  implicit val writes: OWrites[Def1_Retrieve_UkNonFhlPropertyAdjustments] = Json.writes[Def1_Retrieve_UkNonFhlPropertyAdjustments]

  implicit val reads: Reads[Def1_Retrieve_UkNonFhlPropertyAdjustments] = (
    (__ \ "balancingCharge").readNullable[BigDecimal] and
      (__ \ "privateUseAdjustment").readNullable[BigDecimal] and
      (__ \ "businessPremisesRenovationAllowanceBalancingCharges").readNullable[BigDecimal] and
      (__ \ "nonResidentLandlord").read[Boolean] and
      (__ \ "ukOtherRentARoom").readNullable[Def1_Retrieve_UkNonFhlPropertyRentARoom]
  )(Def1_Retrieve_UkNonFhlPropertyAdjustments.apply _)

}
