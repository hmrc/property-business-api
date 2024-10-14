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

package v5.createAmendUkPropertyCumulativeSummary.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMaximum}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import play.api.libs.json.JsValue
import v5.createAmendUkPropertyCumulativeSummary.def1.Def1_CreateAmendUkPropertyCumulativeSummaryValidator.{
  resolveJson,
  resolveTaxYear,
  rulesValidator
}
import v5.createAmendUkPropertyCumulativeSummary.model.request.{
  CreateAmendUkPropertyCumulativeSummaryRequestData,
  Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody,
  Def1_CreateAmendUkPropertyCumulativeSummaryRequestData
}

import javax.inject.Inject

class Def1_CreateAmendUkPropertyCumulativeSummaryValidator @Inject() (nino: String, businessId: String, taxYear: String, body: JsValue)
    extends Validator[CreateAmendUkPropertyCumulativeSummaryRequestData] {

  def validate: Validated[Seq[MtdError], Def1_CreateAmendUkPropertyCumulativeSummaryRequestData] = {
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      ResolveBusinessId(businessId),
      resolveJson(body)
    ).mapN(Def1_CreateAmendUkPropertyCumulativeSummaryRequestData) andThen rulesValidator.validateBusinessRules

  }

}

object Def1_CreateAmendUkPropertyCumulativeSummaryValidator {
  private val maxTaxYear     = TaxYear.fromMtd("2024-25")
  private val resolveTaxYear = ResolveTaxYearMaximum(maxTaxYear)

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def1_CreateAmendUkPropertyCumulativeSummaryRequestBody]()
  private val rulesValidator = new Def1_CreateUkPropertyCumulativeSummaryRulesValidator()

}
