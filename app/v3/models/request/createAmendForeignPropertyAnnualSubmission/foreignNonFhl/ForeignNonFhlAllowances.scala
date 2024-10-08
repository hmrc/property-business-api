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

package v3.models.request.createAmendForeignPropertyAnnualSubmission.foreignNonFhl

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v3.models.request.common.StructuredBuildingAllowance

case class ForeignNonFhlAllowances(annualInvestmentAllowance: Option[BigDecimal],
                                   costOfReplacingDomesticItems: Option[BigDecimal],
                                   zeroEmissionsGoodsVehicleAllowance: Option[BigDecimal],
                                   otherCapitalAllowance: Option[BigDecimal],
                                   electricChargePointAllowance: Option[BigDecimal],
                                   zeroEmissionsCarAllowance: Option[BigDecimal],
                                   propertyIncomeAllowance: Option[BigDecimal],
                                   structuredBuildingAllowance: Option[Seq[StructuredBuildingAllowance]])

object ForeignNonFhlAllowances {
  implicit val reads: Reads[ForeignNonFhlAllowances] = Json.reads[ForeignNonFhlAllowances]

  implicit val writes: Writes[ForeignNonFhlAllowances] = (
    (JsPath \ "annualInvestmentAllowance").writeNullable[BigDecimal] and
      (JsPath \ "costOfReplacingDomesticItems").writeNullable[BigDecimal] and
      (JsPath \ "zeroEmissionsGoodsVehicleAllowance").writeNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").writeNullable[BigDecimal] and
      (JsPath \ "electricChargePointAllowance").writeNullable[BigDecimal] and
      (JsPath \ "zeroEmissionsCarAllowance").writeNullable[BigDecimal] and
      (JsPath \ "propertyAllowance").writeNullable[BigDecimal] and
      (JsPath \ "structuredBuildingAllowance").writeNullable[Seq[StructuredBuildingAllowance]]
  )(unlift(ForeignNonFhlAllowances.unapply))

}
