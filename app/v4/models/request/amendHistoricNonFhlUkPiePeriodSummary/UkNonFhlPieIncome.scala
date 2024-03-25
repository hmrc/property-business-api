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

package v4.models.request.amendHistoricNonFhlUkPiePeriodSummary

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v4.models.request.common.ukPropertyRentARoom.UkPropertyIncomeRentARoom

case class UkNonFhlPieIncome(periodAmount: Option[BigDecimal],
                             premiumsOfLeaseGrant: Option[BigDecimal],
                             reversePremiums: Option[BigDecimal],
                             otherIncome: Option[BigDecimal],
                             taxDeducted: Option[BigDecimal],
                             rentARoom: Option[UkPropertyIncomeRentARoom])

object UkNonFhlPieIncome {

  implicit val reads: Reads[UkNonFhlPieIncome] = Json.reads

  implicit val writes: Writes[UkNonFhlPieIncome] = (
    (JsPath \ "rentIncome" \ "amount").writeNullable[BigDecimal] and
      (JsPath \ "premiumsOfLeaseGrant").writeNullable[BigDecimal] and
      (JsPath \ "reversePremiums").writeNullable[BigDecimal] and
      (JsPath \ "otherIncome").writeNullable[BigDecimal] and
      (JsPath \ "rentIncome" \ "taxDeducted").writeNullable[BigDecimal] and
      (JsPath \ "ukRentARoom").writeNullable[UkPropertyIncomeRentARoom]
  )(unlift(UkNonFhlPieIncome.unapply))

}
