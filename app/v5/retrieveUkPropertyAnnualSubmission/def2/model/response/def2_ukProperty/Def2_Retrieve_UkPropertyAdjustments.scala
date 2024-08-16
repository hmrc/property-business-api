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

package v5.retrieveUkPropertyAnnualSubmission.def2.model.response.def2_ukProperty

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Def2_Retrieve_UkPropertyAdjustments(
    balancingCharge: Option[BigDecimal],
    privateUseAdjustment: Option[BigDecimal],
    businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
    nonResidentLandlord: Boolean,
    rentARoom: Option[Def2_Retrieve_UkPropertyRentARoom]
)

object Def2_Retrieve_UkPropertyAdjustments {
  implicit val writes: OWrites[Def2_Retrieve_UkPropertyAdjustments] = Json.writes[Def2_Retrieve_UkPropertyAdjustments]

  implicit val reads: Reads[Def2_Retrieve_UkPropertyAdjustments] = (
    (__ \ "balancingCharge").readNullable[BigDecimal] and
      (__ \ "privateUseAdjustment").readNullable[BigDecimal] and
      (__ \ "businessPremisesRenovationAllowanceBalancingCharges").readNullable[BigDecimal] and
      (__ \ "nonResidentLandlord").read[Boolean] and
      (__ \ "ukOtherRentARoom").readNullable[Def2_Retrieve_UkPropertyRentARoom]
  )(Def2_Retrieve_UkPropertyAdjustments.apply _)

}