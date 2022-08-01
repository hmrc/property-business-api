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

package v2.models.request.common.ukFhlProperty

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Reads, Writes, __}
import v2.models.request.common.ukPropertyRentARoom.UkPropertyIncomeRentARoom

case class UkFhlPiePropertyIncome(periodAmount: Option[BigDecimal], taxDeducted: Option[BigDecimal], ukRentARoom: Option[UkPropertyIncomeRentARoom])

object UkFhlPiePropertyIncome{

 implicit val reads: Reads[UkFhlPiePropertyIncome] =
  (
    (__ \ "periodAmount").readNullable[BigDecimal] and
    (__ \ "taxDeducted").readNullable[BigDecimal] and
    (__ \ "rentARoom").readNullable[UkPropertyIncomeRentARoom]
  )(UkFhlPiePropertyIncome.apply _)

 implicit val writes: Writes[UkFhlPiePropertyIncome] = (
  (JsPath \ "rentIncome" \ "amount").writeNullable[BigDecimal] and
  (JsPath \ "rentIncome" \ "taxDeducted").writeNullable[BigDecimal] and
  (JsPath \ "ukRentARoom").writeNullable[UkPropertyIncomeRentARoom]
  ) (unlift(UkFhlPiePropertyIncome.unapply))

}