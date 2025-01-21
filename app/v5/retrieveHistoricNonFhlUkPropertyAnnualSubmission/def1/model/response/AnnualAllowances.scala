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

case class AnnualAllowances(annualInvestmentAllowance: Option[BigDecimal],
                            otherCapitalAllowance: Option[BigDecimal],
                            zeroEmissionGoodsVehicleAllowance: Option[BigDecimal],
                            businessPremisesRenovationAllowance: Option[BigDecimal],
                            costOfReplacingDomesticGoods: Option[BigDecimal],
                            propertyIncomeAllowance: Option[BigDecimal])

object AnnualAllowances {
  implicit val writes: OWrites[AnnualAllowances] = Json.writes[AnnualAllowances]

  implicit val reads: Reads[AnnualAllowances] = (
    (JsPath \ "annualInvestmentAllowance").readNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").readNullable[BigDecimal] and
      (JsPath \ "zeroEmissionGoodsVehicleAllowance").readNullable[BigDecimal] and
      (JsPath \ "businessPremisesRenovationAllowance").readNullable[BigDecimal] and
      (JsPath \ "costOfReplacingDomGoods").readNullable[BigDecimal] and
      (JsPath \ "propertyIncomeAllowance").readNullable[BigDecimal]
  )(AnnualAllowances.apply _)

}
