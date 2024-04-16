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

package v4.controllers.amendUkPropertyPeriodSummary.def2.model.request.def2_ukFhlProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v4.controllers.amendUkPropertyPeriodSummary.def2.model.request.def2_ukPropertyRentARoom.Def2_Amend_UkPropertyIncomeRentARoom

case class Def2_Amend_UkFhlPropertyIncome(periodAmount: Option[BigDecimal],
                                          taxDeducted: Option[BigDecimal],
                                          rentARoom: Option[Def2_Amend_UkPropertyIncomeRentARoom])

object Def2_Amend_UkFhlPropertyIncome {
  implicit val reads: Reads[Def2_Amend_UkFhlPropertyIncome] = Json.reads[Def2_Amend_UkFhlPropertyIncome]

  implicit val writes: Writes[Def2_Amend_UkFhlPropertyIncome] = (
    (JsPath \ "periodAmount").writeNullable[BigDecimal] and
      (JsPath \ "taxDeducted").writeNullable[BigDecimal] and
      (JsPath \ "ukFhlRentARoom").writeNullable[Def2_Amend_UkPropertyIncomeRentARoom]
  )(unlift(Def2_Amend_UkFhlPropertyIncome.unapply))

}
