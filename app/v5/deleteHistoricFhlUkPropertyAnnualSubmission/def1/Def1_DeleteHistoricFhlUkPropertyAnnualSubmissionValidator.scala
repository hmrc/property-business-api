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

package v5.deleteHistoricFhlUkPropertyAnnualSubmission.def1

import cats.data.Validated
import cats.data.Validated.*
import cats.implicits.*
import common.models.domain.HistoricPropertyType
import common.models.errors.RuleHistoricTaxYearNotSupportedError
import config.PropertyBusinessConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinMax}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v5.deleteHistoricFhlUkPropertyAnnualSubmission.model.request.*

import javax.inject.Inject

class Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionValidator @Inject() (nino: String, taxYear: String, propertyType: HistoricPropertyType)(implicit
    config: PropertyBusinessConfig)
    extends Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinMax(
    (TaxYear.fromMtd(config.historicMinimumTaxYear), TaxYear.fromMtd(config.historicMaximumTaxYear)),
    RuleHistoricTaxYearNotSupportedError)

  def validate: Validated[Seq[MtdError], DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      Valid(propertyType)
    ).mapN(Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData)

}
