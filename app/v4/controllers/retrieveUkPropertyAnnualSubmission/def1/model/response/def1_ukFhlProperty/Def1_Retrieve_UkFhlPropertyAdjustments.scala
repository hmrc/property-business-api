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

package v4.controllers.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukFhlProperty

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Def1_Retrieve_UkFhlPropertyAdjustments(
    privateUseAdjustment: Option[BigDecimal],
    balancingCharge: Option[BigDecimal],
    periodOfGraceAdjustment: Boolean,
    businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
    nonResidentLandlord: Boolean,
    rentARoom: Option[Def1_Retrieve_UkFhlPropertyRentARoom]
)

object Def1_Retrieve_UkFhlPropertyAdjustments {
  implicit val writes: OWrites[Def1_Retrieve_UkFhlPropertyAdjustments] = Json.writes[Def1_Retrieve_UkFhlPropertyAdjustments]

  implicit val reads: Reads[Def1_Retrieve_UkFhlPropertyAdjustments] = (
    (__ \ "privateUseAdjustment").readNullable[BigDecimal] and
      (__ \ "balancingCharge").readNullable[BigDecimal] and
      (__ \ "periodOfGraceAdjustment").read[Boolean] and
      (__ \ "businessPremisesRenovationAllowanceBalancingCharges").readNullable[BigDecimal] and
      (__ \ "nonResidentLandlord").read[Boolean] and
      (__ \ "ukFhlRentARoom").readNullable[Def1_Retrieve_UkFhlPropertyRentARoom]
  )(Def1_Retrieve_UkFhlPropertyAdjustments.apply _)

}
