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

package v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.def1.model.request

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class HistoricNonFhlAnnualAllowances(
    annualInvestmentAllowance: Option[BigDecimal],
    zeroEmissionGoodsVehicleAllowance: Option[BigDecimal],
    businessPremisesRenovationAllowance: Option[BigDecimal],
    otherCapitalAllowance: Option[BigDecimal],
    costOfReplacingDomesticGoods: Option[BigDecimal],
    propertyIncomeAllowance: Option[BigDecimal]
)

object HistoricNonFhlAnnualAllowances {
  implicit val reads: Reads[HistoricNonFhlAnnualAllowances] = Json.reads[HistoricNonFhlAnnualAllowances]

  implicit val writes: OWrites[HistoricNonFhlAnnualAllowances] = (
    (JsPath \ "annualInvestmentAllowance").writeNullable[BigDecimal] and
      (JsPath \ "zeroEmissionGoodsVehicleAllowance").writeNullable[BigDecimal] and
      (JsPath \ "businessPremisesRenovationAllowance").writeNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").writeNullable[BigDecimal] and
      (JsPath \ "costOfReplacingDomGoods").writeNullable[BigDecimal] and
      (JsPath \ "propertyIncomeAllowance").writeNullable[BigDecimal]
  )(o => Tuple.fromProductTyped(o))

}
