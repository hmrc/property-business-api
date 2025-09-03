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

package v6.retrieveForeignPropertyAnnualSubmission.def2.model.response

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveForeignPropertyAllowances(annualInvestmentAllowance: Option[BigDecimal],
                                             costOfReplacingDomesticItems: Option[BigDecimal],
                                             otherCapitalAllowance: Option[BigDecimal],
                                             zeroEmissionsCarAllowance: Option[BigDecimal],
                                             propertyIncomeAllowance: Option[BigDecimal],
                                             structuredBuildingAllowance: Option[Seq[RetrieveStructuredBuildingAllowance]])

object RetrieveForeignPropertyAllowances {
  implicit val writes: OWrites[RetrieveForeignPropertyAllowances] = Json.writes[RetrieveForeignPropertyAllowances]

  implicit val reads: Reads[RetrieveForeignPropertyAllowances] = (
    (JsPath \ "annualInvestmentAllowance").readNullable[BigDecimal] and
      (JsPath \ "costOfReplacingDomesticItems").readNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").readNullable[BigDecimal] and
      (JsPath \ "zeroEmissionsCarAllowance").readNullable[BigDecimal] and
      (JsPath \ "propertyAllowance").readNullable[BigDecimal] and
      (JsPath \ "structuredBuildingAllowance").readNullable[Seq[RetrieveStructuredBuildingAllowance]]
  )(RetrieveForeignPropertyAllowances.apply _)

}
