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

package v5.retrieveHistoricNonFhlUkPropertyAnnualSubmission.def1

import cats.data.Validated
import cats.implicits._
import common.models.errors.RuleHistoricTaxYearNotSupportedError
import config.PropertyBusinessConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYearMinMax}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v5.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}

import javax.inject.Inject

class Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidator @Inject() (nino: String, taxYear: String)(implicit
    config: PropertyBusinessConfig)
    extends Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinMax(
    (TaxYear.fromMtd(config.historicMinimumTaxYear), TaxYear.fromMtd(config.historicMaximumTaxYear)),
    RuleHistoricTaxYearNotSupportedError)

  def validate: Validated[Seq[MtdError], RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear)
    ).mapN(Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData)

}
