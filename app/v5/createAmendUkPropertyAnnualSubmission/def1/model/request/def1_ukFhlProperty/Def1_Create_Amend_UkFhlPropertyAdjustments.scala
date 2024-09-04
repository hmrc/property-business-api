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

package v5.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukFhlProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v5.createAmendUkPropertyAnnualSubmission.def1.model.request.def1_ukPropertyRentARoom.Def1_Create_Amend_UkPropertyAdjustmentsRentARoom

case class Def1_Create_Amend_UkFhlPropertyAdjustments(privateUseAdjustment: Option[BigDecimal],
                                                      balancingCharge: Option[BigDecimal],
                                                      periodOfGraceAdjustment: Boolean,
                                                      businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
                                                      nonResidentLandlord: Boolean,
                                                      rentARoom: Option[Def1_Create_Amend_UkPropertyAdjustmentsRentARoom])

object Def1_Create_Amend_UkFhlPropertyAdjustments {
  implicit val reads: Reads[Def1_Create_Amend_UkFhlPropertyAdjustments] = Json.reads[Def1_Create_Amend_UkFhlPropertyAdjustments]

  implicit val writes: Writes[Def1_Create_Amend_UkFhlPropertyAdjustments] = (
    (JsPath \ "privateUseAdjustment").writeNullable[BigDecimal] and
      (JsPath \ "balancingCharge").writeNullable[BigDecimal] and
      (JsPath \ "periodOfGraceAdjustment").write[Boolean] and
      (JsPath \ "businessPremisesRenovationAllowanceBalancingCharges").writeNullable[BigDecimal] and
      (JsPath \ "nonResidentLandlord").write[Boolean] and
      (JsPath \ "ukFhlRentARoom").writeNullable[Def1_Create_Amend_UkPropertyAdjustmentsRentARoom]
  )(unlift(Def1_Create_Amend_UkFhlPropertyAdjustments.unapply))

}
