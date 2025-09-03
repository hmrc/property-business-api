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

package v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveUkPropertyAllowances(
    annualInvestmentAllowance: Option[BigDecimal],
    zeroEmissionsGoodsVehicleAllowance: Option[BigDecimal],
    businessPremisesRenovationAllowance: Option[BigDecimal],
    otherCapitalAllowance: Option[BigDecimal],
    costOfReplacingDomesticItems: Option[BigDecimal],
    propertyIncomeAllowance: Option[BigDecimal],
    electricChargePointAllowance: Option[BigDecimal],
    structuredBuildingAllowance: Option[Seq[RetrieveUkPropertyStructuredBuildingAllowance]],
    enhancedStructuredBuildingAllowance: Option[Seq[RetrieveUkPropertyStructuredBuildingAllowance]],
    zeroEmissionsCarAllowance: Option[BigDecimal]
)

object RetrieveUkPropertyAllowances {

  implicit val writes: OWrites[RetrieveUkPropertyAllowances] = Json.writes

  implicit val reads: Reads[RetrieveUkPropertyAllowances] = (
    (JsPath \ "annualInvestmentAllowance").readNullable[BigDecimal] and
      (JsPath \ "zeroEmissionGoodsVehicleAllowance").readNullable[BigDecimal] and
      (JsPath \ "businessPremisesRenovationAllowance").readNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").readNullable[BigDecimal] and
      ((JsPath \ "costOfReplacingDomesticItems").read[BigDecimal].map(Option(_)) orElse (JsPath \ "costOfReplacingDomesticGoods")
        .readNullable[BigDecimal]) and // orElse implemented due to downstream bug specified here - MTDSA-22775
      (JsPath \ "propertyIncomeAllowance").readNullable[BigDecimal] and
      (JsPath \ "electricChargePointAllowance").readNullable[BigDecimal] and
      (JsPath \ "structuredBuildingAllowance").readNullable[Seq[RetrieveUkPropertyStructuredBuildingAllowance]] and
      (JsPath \ "enhancedStructuredBuildingAllowance").readNullable[Seq[RetrieveUkPropertyStructuredBuildingAllowance]] and
      (JsPath \ "zeroEmissionsCarAllowance").readNullable[BigDecimal]
  )(RetrieveUkPropertyAllowances.apply _)

}
