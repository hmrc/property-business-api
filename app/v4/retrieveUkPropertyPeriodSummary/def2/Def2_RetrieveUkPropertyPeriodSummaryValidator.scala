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

package v4.retrieveUkPropertyPeriodSummary.def2

import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import common.controllers.validators.resolvers.ResolveSubmissionId
import config.PropertyBusinessConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYearMinMax}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v4.retrieveUkPropertyPeriodSummary.model.request.{Def2_RetrieveUkPropertyPeriodSummaryRequestData, RetrieveUkPropertyPeriodSummaryRequestData}

import javax.inject.Inject

class Def2_RetrieveUkPropertyPeriodSummaryValidator @Inject() (
    nino: String,
    businessId: String,
    taxYear: String,
    submissionId: String
)(implicit config: PropertyBusinessConfig)
    extends Validator[RetrieveUkPropertyPeriodSummaryRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinMax((TaxYear.fromMtd(config.ukMinimumTaxYear), TaxYear.fromMtd("2024-25")))

  def validate: Validated[Seq[MtdError], RetrieveUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear),
      ResolveSubmissionId(submissionId)
    ).mapN(Def2_RetrieveUkPropertyPeriodSummaryRequestData)

}
