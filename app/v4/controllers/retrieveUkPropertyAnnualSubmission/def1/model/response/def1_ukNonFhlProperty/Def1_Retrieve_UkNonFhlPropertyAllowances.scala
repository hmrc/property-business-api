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

package v4.controllers.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukNonFhlProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Def1_Retrieve_UkNonFhlPropertyAllowances(
    annualInvestmentAllowance: Option[BigDecimal],
    zeroEmissionsGoodsVehicleAllowance: Option[BigDecimal],
    businessPremisesRenovationAllowance: Option[BigDecimal],
    otherCapitalAllowance: Option[BigDecimal],
    costOfReplacingDomesticGoods: Option[BigDecimal],
    propertyIncomeAllowance: Option[BigDecimal],
    electricChargePointAllowance: Option[BigDecimal],
    structuredBuildingAllowance: Option[Seq[Def1_Retrieve_UkNonFhlPropertyStructuredBuildingAllowance]],
    enhancedStructuredBuildingAllowance: Option[Seq[Def1_Retrieve_UkNonFhlPropertyStructuredBuildingAllowance]],
    zeroEmissionsCarAllowance: Option[BigDecimal]
)

object Def1_Retrieve_UkNonFhlPropertyAllowances {
  implicit val writes: OWrites[Def1_Retrieve_UkNonFhlPropertyAllowances] = Json.writes[Def1_Retrieve_UkNonFhlPropertyAllowances]

  implicit val reads: Reads[Def1_Retrieve_UkNonFhlPropertyAllowances] = (
    (JsPath \ "annualInvestmentAllowance").readNullable[BigDecimal] and
      (JsPath \ "zeroEmissionGoodsVehicleAllowance").readNullable[BigDecimal] and
      (JsPath \ "businessPremisesRenovationAllowance").readNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").readNullable[BigDecimal] and
      (JsPath \ "costOfReplacingDomesticGoods").readNullable[BigDecimal] and
      (JsPath \ "propertyIncomeAllowance").readNullable[BigDecimal] and
      (JsPath \ "electricChargePointAllowance").readNullable[BigDecimal] and
      (JsPath \ "structuredBuildingAllowance").readNullable[Seq[Def1_Retrieve_UkNonFhlPropertyStructuredBuildingAllowance]] and
      (JsPath \ "enhancedStructuredBuildingAllowance").readNullable[Seq[Def1_Retrieve_UkNonFhlPropertyStructuredBuildingAllowance]] and
      (JsPath \ "zeroEmissionsCarAllowance").readNullable[BigDecimal]
  )(Def1_Retrieve_UkNonFhlPropertyAllowances.apply _)

}
