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

package v6.createAmendForeignPropertyCumulativePeriodSummary.def1

import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYear}
import shared.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import play.api.libs.json.JsValue
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRulesValidator.validateBusinessRules
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.model.request.{
  Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody,
  Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData
}
import v6.createAmendForeignPropertyCumulativePeriodSummary.model.request._

class Def1_CreateAmendForeignPropertyCumulativePeriodSummaryValidator(nino: String, businessId: String, taxYear: String, body: JsValue)
    extends Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestBody]()

  def validate: Validated[Seq[MtdError], CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData) andThen validateBusinessRules

}
