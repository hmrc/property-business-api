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

package v5.createAmendUkPropertyAnnualSubmission.def2.model.request

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class Allowances(annualInvestmentAllowance: Option[BigDecimal],
                      businessPremisesRenovationAllowance: Option[BigDecimal],
                      otherCapitalAllowance: Option[BigDecimal],
                      costOfReplacingDomesticItems: Option[BigDecimal],
                      zeroEmissionsCarAllowance: Option[BigDecimal],
                      propertyIncomeAllowance: Option[BigDecimal],
                      structuredBuildingAllowance: Option[Seq[StructuredBuildingAllowance]],
                      enhancedStructuredBuildingAllowance: Option[Seq[StructuredBuildingAllowance]])

object Allowances {
  implicit val reads: Reads[Allowances] = Json.reads[Allowances]

  implicit val writes: Writes[Allowances] = (
    (JsPath \ "annualInvestmentAllowance").writeNullable[BigDecimal] and
      (JsPath \ "businessPremisesRenovationAllowance").writeNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").writeNullable[BigDecimal] and
      (JsPath \ "costOfReplacingDomesticGoods").writeNullable[BigDecimal] and
      (JsPath \ "zeroEmissionsCarAllowance").writeNullable[BigDecimal] and
      (JsPath \ "propertyIncomeAllowance").writeNullable[BigDecimal] and
      (JsPath \ "structuredBuildingAllowance").writeNullable[Seq[StructuredBuildingAllowance]] and
      (JsPath \ "enhancedStructuredBuildingAllowance").writeNullable[Seq[StructuredBuildingAllowance]]
  )(o => Tuple.fromProductTyped(o))

}
