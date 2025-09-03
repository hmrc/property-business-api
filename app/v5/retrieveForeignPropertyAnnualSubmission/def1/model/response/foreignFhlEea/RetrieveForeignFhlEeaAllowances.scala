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

package v5.retrieveForeignPropertyAnnualSubmission.def1.model.response.foreignFhlEea

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveForeignFhlEeaAllowances(annualInvestmentAllowance: Option[BigDecimal],
                                           otherCapitalAllowance: Option[BigDecimal],
                                           electricChargePointAllowance: Option[BigDecimal],
                                           zeroEmissionsCarAllowance: Option[BigDecimal],
                                           propertyIncomeAllowance: Option[BigDecimal])

object RetrieveForeignFhlEeaAllowances {
  implicit val writes: OWrites[RetrieveForeignFhlEeaAllowances] = Json.writes[RetrieveForeignFhlEeaAllowances]

  implicit val reads: Reads[RetrieveForeignFhlEeaAllowances] = (
    (JsPath \ "annualInvestmentAllowance").readNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").readNullable[BigDecimal] and
      (JsPath \ "electricChargePointAllowance").readNullable[BigDecimal] and
      (JsPath \ "zeroEmissionsCarAllowance").readNullable[BigDecimal] and
      (JsPath \ "propertyAllowance").readNullable[BigDecimal]
  )(RetrieveForeignFhlEeaAllowances.apply _)

}
