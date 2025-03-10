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

package v6.retrieveUkPropertyAnnualSubmission.def2.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class RetrieveUkPropertyAdjustments(
    balancingCharge: Option[BigDecimal],
    privateUseAdjustment: Option[BigDecimal],
    businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
    nonResidentLandlord: Boolean,
    rentARoom: Option[RetrieveUkPropertyRentARoom]
)

object RetrieveUkPropertyAdjustments {
  implicit val writes: OWrites[RetrieveUkPropertyAdjustments] = Json.writes[RetrieveUkPropertyAdjustments]

  implicit val reads: Reads[RetrieveUkPropertyAdjustments] = (
    (__ \ "balancingCharge").readNullable[BigDecimal] and
      (__ \ "privateUseAdjustment").readNullable[BigDecimal] and
      (__ \ "businessPremisesRenovationAllowanceBalancingCharges").readNullable[BigDecimal] and
      (__ \ "nonResidentLandlord").read[Boolean] and
      (__ \ "rentARoom").readNullable[RetrieveUkPropertyRentARoom]
  )(RetrieveUkPropertyAdjustments.apply _)

}
