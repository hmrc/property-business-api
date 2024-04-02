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

package v4.models.request.createAmendForeignPropertyAnnualSubmission.foreignFhlEea

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class ForeignFhlEeaAllowances(annualInvestmentAllowance: Option[BigDecimal],
                                   otherCapitalAllowance: Option[BigDecimal],
                                   electricChargePointAllowance: Option[BigDecimal],
                                   zeroEmissionsCarAllowance: Option[BigDecimal],
                                   propertyIncomeAllowance: Option[BigDecimal])

object ForeignFhlEeaAllowances {
  implicit val reads: Reads[ForeignFhlEeaAllowances] = Json.reads[ForeignFhlEeaAllowances]

  implicit val writes: Writes[ForeignFhlEeaAllowances] = (
    (JsPath \ "annualInvestmentAllowance").writeNullable[BigDecimal] and
      (JsPath \ "otherCapitalAllowance").writeNullable[BigDecimal] and
      (JsPath \ "electricChargePointAllowance").writeNullable[BigDecimal] and
      (JsPath \ "zeroEmissionsCarAllowance").writeNullable[BigDecimal] and
      (JsPath \ "propertyAllowance").writeNullable[BigDecimal]
  )(unlift(ForeignFhlEeaAllowances.unapply))

}
