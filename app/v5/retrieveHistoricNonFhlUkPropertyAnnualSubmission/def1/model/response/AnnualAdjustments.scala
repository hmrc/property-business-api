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

package v5.retrieveHistoricNonFhlUkPropertyAnnualSubmission.def1.model.response

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RentARoom(jointlyLet: Boolean)

object RentARoom {
  implicit val writes: OWrites[RentARoom] = Json.writes[RentARoom]

  implicit val reads: Reads[RentARoom] = Json.reads[RentARoom]
}

case class AnnualAdjustments(lossBroughtForward: Option[BigDecimal],
                             balancingCharge: Option[BigDecimal],
                             privateUseAdjustment: Option[BigDecimal],
                             businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
                             nonResidentLandlord: Boolean,
                             rentARoom: Option[RentARoom])

object AnnualAdjustments {
  implicit val writes: OWrites[AnnualAdjustments] = Json.writes[AnnualAdjustments]

  implicit val reads: Reads[AnnualAdjustments] = (
    (JsPath \ "lossBroughtForward").readNullable[BigDecimal] and
      (JsPath \ "balancingCharge").readNullable[BigDecimal] and
      (JsPath \ "privateUseAdjustment").readNullable[BigDecimal] and
      (JsPath \ "businessPremisesRenovationAllowanceBalancingCharges").readNullable[BigDecimal] and
      (JsPath \ "nonResidentLandlord").read[Boolean] and
      (JsPath \ "ukRentARoom").readNullable[RentARoom]
  )(AnnualAdjustments.apply _)

}
