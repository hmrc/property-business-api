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

package v2.models.request.amendHistoricFhlUkPropertyAnnualSubmission.historicFhl

import play.api.libs.json.{Json, OFormat}

case class HistoricFhlAnnualAdjustments(lossBoughtForward: Option[BigDecimal],
                                        privateUseAdjustment: Option[BigDecimal],
                                        balancingCharge: Option[BigDecimal],
                                        periodOfGraceAdjustment: Boolean,
                                        businessPremisesRenovationAllowanceBalancingCharges: Option[BigDecimal],
                                        nonResidentLandlord: Boolean,
                                        rentARoom: Option[UkPropertyAdjustmentsRentARoom])

object HistoricFhlAnnualAdjustments {
  implicit val format: OFormat[HistoricFhlAnnualAdjustments] = Json.format[HistoricFhlAnnualAdjustments]

  implicit val writes: Writes[HistoricFhlAnnualAdjustments] = (
    (JsPath \ "lossBoughtForward").writeNullable[BigDecimal] and
      (JsPath \ "privateUseAdjustment").writeNullable[BigDecimal] and
      (JsPath \ "balancingCharge").writeNullable[BigDecimal] and
      (JsPath \ "periodOfGraceAdjustment").write[Boolean] and
      (JsPath \ "businessPremisesRenovationAllowanceBalancingCharges").writeNullable[BigDecimal] and
      (JsPath \ "nonResidentLandlord").write[Boolean] and
      (JsPath \ "rentARoom").writeNullable[UkPropertyAdjustmentsRentARoom]
    ) (unlift(HistoricFhlAnnualAdjustments.unapply))
}
