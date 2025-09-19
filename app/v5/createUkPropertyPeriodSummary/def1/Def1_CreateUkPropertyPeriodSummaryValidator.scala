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

package v5.createUkPropertyPeriodSummary.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import config.PropertyBusinessConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMinMax}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v5.createUkPropertyPeriodSummary.model.request.*

import javax.inject.Inject

class Def1_CreateUkPropertyPeriodSummaryValidator @Inject() (nino: String, businessId: String, taxYear: String, body: JsValue)(implicit
    config: PropertyBusinessConfig)
    extends Validator[CreateUkPropertyPeriodSummaryRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinMax((TaxYear.fromMtd(config.ukMinimumTaxYear), TaxYear.fromMtd("2023-24")))

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def1_CreateUkPropertyPeriodSummaryRequestBody]()
  private val rulesValidator = new Def1_CreateUkPropertyPeriodSummaryRulesValidator()

  def validate: Validated[Seq[MtdError], CreateUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateUkPropertyPeriodSummaryRequestData.apply) andThen rulesValidator.validateBusinessRules

}
