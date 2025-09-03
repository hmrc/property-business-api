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

package v6.createAmendUkPropertyAnnualSubmission.def1.model.request.ukProperty

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v6.createAmendUkPropertyAnnualSubmission.def1.model.request.ukPropertyRentARoom.CreateAmendUkPropertyAdjustmentsRentARoom

case class CreateAmendUkPropertyAdjustments(balancingCharge: Option[BigDecimal],
                                            privateUseAdjustment: Option[BigDecimal],
                                            businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
                                            nonResidentLandlord: Boolean,
                                            rentARoom: Option[CreateAmendUkPropertyAdjustmentsRentARoom])

object CreateAmendUkPropertyAdjustments {
  implicit val reads: Reads[CreateAmendUkPropertyAdjustments] = Json.reads[CreateAmendUkPropertyAdjustments]

  implicit val writes: Writes[CreateAmendUkPropertyAdjustments] = (
    (JsPath \ "balancingCharge").writeNullable[BigDecimal] and
      (JsPath \ "privateUseAdjustment").writeNullable[BigDecimal] and
      (JsPath \ "businessPremisesRenovationAllowanceBalancingCharges").writeNullable[BigDecimal] and
      (JsPath \ "nonResidentLandlord").write[Boolean] and
      (JsPath \ "ukOtherRentARoom").writeNullable[CreateAmendUkPropertyAdjustmentsRentARoom]
  )(o => Tuple.fromProductTyped(o))

}
