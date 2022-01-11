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

package v2.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import play.api.libs.functional.syntax._

case class UkNonFhlPropertyAllowances(
    annualInvestmentAllowance: Option[BigDecimal],
    zeroEmissionsGoodsVehicleAllowance: Option[BigDecimal],
    businessPremisesRenovationAllowance: Option[BigDecimal],
    otherCapitalAllowance: Option[BigDecimal],
    costOfReplacingDomesticGoods: Option[BigDecimal],
    propertyIncomeAllowance: Option[BigDecimal],
    electricChargePointAllowance: Option[BigDecimal],
    structuredBuildingAllowance: Option[Seq[UkNonFhlPropertyStructuredBuildingAllowance]],
    enhancedStructuredBuildingAllowance: Option[Seq[UkNonFhlPropertyStructuredBuildingAllowance]],
    zeroEmissionsCarAllowance: Option[BigDecimal],
)

object UkNonFhlPropertyAllowances {
  implicit  val writes: OWrites[UkNonFhlPropertyAllowances] = Json.writes[UkNonFhlPropertyAllowances]

  implicit val reads: Reads[UkNonFhlPropertyAllowances] = (
    (JsPath \ "annualInvestmentAllowance").readNullable[BigDecimal] and
      (JsPath \ "zeroEmissionGoodsVehicleAllowance").readNullable[BigDecimal] and
      (JsPath \ "businessPremisesRenovationAllowance").readNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").readNullable[BigDecimal] and
      (JsPath \ "costOfReplacingDomesticGoods").readNullable[BigDecimal] and
      (JsPath \ "propertyIncomeAllowance").readNullable[BigDecimal] and
      (JsPath \ "electricChargePointAllowance").readNullable[BigDecimal] and
      (JsPath \ "structuredBuildingAllowance").readNullable[Seq[UkNonFhlPropertyStructuredBuildingAllowance]] and
      (JsPath \ "enhancedStructuredBuildingAllowance").readNullable[Seq[UkNonFhlPropertyStructuredBuildingAllowance]] and
      (JsPath \ "zeroEmissionsCarAllowance").readNullable[BigDecimal]
    )(UkNonFhlPropertyAllowances.apply _)
}
