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

package v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.def3_foreignProperty

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class Def3_Create_Amend_ForeignAllowances(annualInvestmentAllowance: Option[BigDecimal],
                                               costOfReplacingDomesticItems: Option[BigDecimal],
                                               otherCapitalAllowance: Option[BigDecimal],
                                               zeroEmissionsCarAllowance: Option[BigDecimal],
                                               propertyIncomeAllowance: Option[BigDecimal],
                                               structuredBuildingAllowance: Option[Seq[Def3_Create_Amend_StructuredBuildingAllowance]])

object Def3_Create_Amend_ForeignAllowances {
  implicit val reads: Reads[Def3_Create_Amend_ForeignAllowances] = Json.reads[Def3_Create_Amend_ForeignAllowances]

  implicit val writes: Writes[Def3_Create_Amend_ForeignAllowances] = (
    (JsPath \ "annualInvestmentAllowance").writeNullable[BigDecimal] and
      (JsPath \ "costOfReplacingDomesticItems").writeNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").writeNullable[BigDecimal] and
      (JsPath \ "zeroEmissionsCarAllowance").writeNullable[BigDecimal] and
      (JsPath \ "propertyAllowance").writeNullable[BigDecimal] and
      (JsPath \ "structuredBuildingAllowance").writeNullable[Seq[Def3_Create_Amend_StructuredBuildingAllowance]]
  )(o => Tuple.fromProductTyped(o))

}
