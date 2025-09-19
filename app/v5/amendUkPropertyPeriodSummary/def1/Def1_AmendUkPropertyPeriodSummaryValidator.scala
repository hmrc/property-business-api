/*
 * Copyright 2025 HM Revenue & Customs
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

package v5.amendUkPropertyPeriodSummary.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple5Semigroupal
import common.controllers.validators.resolvers.ResolveSubmissionId
import config.PropertyBusinessConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.*
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v5.amendUkPropertyPeriodSummary.model.request.*

import javax.inject.Inject

class Def1_AmendUkPropertyPeriodSummaryValidator @Inject() (nino: String, businessId: String, taxYear: String, submissionId: String, body: JsValue)(
    implicit config: PropertyBusinessConfig)
    extends Validator[AmendUkPropertyPeriodSummaryRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromMtd(config.ukMinimumTaxYear))

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def1_AmendUkPropertyPeriodSummaryRequestBody]()
  private val rulesValidator = new Def1_AmendUkPropertyPeriodSummaryRulesValidator()

  def validate: Validated[Seq[MtdError], AmendUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      ResolveBusinessId(businessId),
      ResolveSubmissionId(submissionId),
      resolveJson(body)
    ).mapN(Def1_AmendUkPropertyPeriodSummaryRequestData.apply) andThen rulesValidator.validateBusinessRules

}
