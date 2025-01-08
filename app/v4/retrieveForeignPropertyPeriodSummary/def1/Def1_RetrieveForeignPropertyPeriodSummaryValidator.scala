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

package v4.retrieveForeignPropertyPeriodSummary.def1

import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import common.controllers.validators.resolvers.{ResolveSubmissionId, ResolveTaxYear}
import config.AppConfig
import v4.retrieveForeignPropertyPeriodSummary.model.request._

class Def1_RetrieveForeignPropertyPeriodSummaryValidator(nino: String,
                                                         businessId: String,
                                                         taxYear: String,
                                                         maximumTaxYear: TaxYear,
                                                         submissionId: String,
                                                         appConfig: AppConfig)
    extends Validator[RetrieveForeignPropertyPeriodSummaryRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxV2Foreign

  def validate: Validated[Seq[MtdError], RetrieveForeignPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear, minimumTaxYear, maximumTaxYear),
      ResolveSubmissionId(submissionId)
    ).mapN(Def1_RetrieveForeignPropertyPeriodSummaryRequestData)

}
