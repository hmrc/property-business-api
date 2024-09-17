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

package v4.retrieveForeignPropertyAnnualSubmission.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYear}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import v4.retrieveForeignPropertyAnnualSubmission.model.request.{
  Def1_RetrieveForeignPropertyAnnualSubmissionRequestData,
  RetrieveForeignPropertyAnnualSubmissionRequestData
}

import javax.inject.Inject

class Def1_RetrieveForeignPropertyAnnualSubmissionValidator @Inject() (nino: String, businessId: String, taxYear: String)(appConfig: AppConfig)
    extends Validator[RetrieveForeignPropertyAnnualSubmissionRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxV2Foreign
  private lazy val maximumTaxYear = TaxYear.fromMtd("2024-25")

  def validate: Validated[Seq[MtdError], RetrieveForeignPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear, minimumTaxYear, maximumTaxYear)
    ).mapN(Def1_RetrieveForeignPropertyAnnualSubmissionRequestData)

}
