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

package v4.retrieveUkPropertyAnnualSubmission.def2.model.response.def2_ukProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Def2_Retrieve_UkPropertyAllowances(
    annualInvestmentAllowance: Option[BigDecimal],
    zeroEmissionsGoodsVehicleAllowance: Option[BigDecimal],
    businessPremisesRenovationAllowance: Option[BigDecimal],
    otherCapitalAllowance: Option[BigDecimal],
    costOfReplacingDomesticGoods: Option[BigDecimal],
    propertyIncomeAllowance: Option[BigDecimal],
    electricChargePointAllowance: Option[BigDecimal],
    structuredBuildingAllowance: Option[Seq[Def2_Retrieve_UkPropertyStructuredBuildingAllowance]],
    enhancedStructuredBuildingAllowance: Option[Seq[Def2_Retrieve_UkPropertyStructuredBuildingAllowance]],
    zeroEmissionsCarAllowance: Option[BigDecimal]
)

object Def2_Retrieve_UkPropertyAllowances {
  implicit val writes: OWrites[Def2_Retrieve_UkPropertyAllowances] = Json.writes[Def2_Retrieve_UkPropertyAllowances]

  implicit val reads: Reads[Def2_Retrieve_UkPropertyAllowances] = (
    (JsPath \ "annualInvestmentAllowance").readNullable[BigDecimal] and
      (JsPath \ "zeroEmissionGoodsVehicleAllowance").readNullable[BigDecimal] and
      (JsPath \ "businessPremisesRenovationAllowance").readNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").readNullable[BigDecimal] and
      ((JsPath \ "costOfReplacingDomesticItems").read[BigDecimal].map(Option(_)) orElse (JsPath \ "costOfReplacingDomesticGoods")
        .readNullable[BigDecimal]) and // orElse implemented due to downstream bug specified here - MTDSA-22775
      (JsPath \ "propertyIncomeAllowance").readNullable[BigDecimal] and
      (JsPath \ "electricChargePointAllowance").readNullable[BigDecimal] and
      (JsPath \ "structuredBuildingAllowance").readNullable[Seq[Def2_Retrieve_UkPropertyStructuredBuildingAllowance]] and
      (JsPath \ "enhancedStructuredBuildingAllowance").readNullable[Seq[Def2_Retrieve_UkPropertyStructuredBuildingAllowance]] and
      (JsPath \ "zeroEmissionsCarAllowance").readNullable[BigDecimal]
  )(Def2_Retrieve_UkPropertyAllowances.apply _)

}
