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

package v6.retrieveForeignPropertyPeriodSummary.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import common.controllers.validators.resolvers.ResolveSubmissionId
import config.PropertyBusinessConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYearMinMax}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v6.retrieveForeignPropertyPeriodSummary.model.request.*

import javax.inject.Inject

class Def1_RetrieveForeignPropertyPeriodSummaryValidator @Inject() (nino: String,
                                                                    businessId: String,
                                                                    taxYear: String,
                                                                    maximumTaxYear: TaxYear,
                                                                    submissionId: String)(implicit config: PropertyBusinessConfig)
    extends Validator[RetrieveForeignPropertyPeriodSummaryRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinMax((TaxYear.fromMtd(config.foreignMinimumTaxYear), maximumTaxYear))

  def validate: Validated[Seq[MtdError], RetrieveForeignPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear),
      ResolveSubmissionId(submissionId)
    ).mapN(Def1_RetrieveForeignPropertyPeriodSummaryRequestData.apply)

}
