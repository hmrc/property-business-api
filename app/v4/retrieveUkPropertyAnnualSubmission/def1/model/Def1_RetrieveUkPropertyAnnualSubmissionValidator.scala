/*
 * Copyright 2024 HM Revenue & Customs
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

package v4.retrieveUkPropertyAnnualSubmission.def1.model

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYear}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import config.AppConfig
import v4.retrieveUkPropertyAnnualSubmission.model.request._

import javax.inject.Inject

class Def1_RetrieveUkPropertyAnnualSubmissionValidator @Inject() (nino: String, businessId: String, taxYear: String)(appConfig: AppConfig)
    extends Validator[RetrieveUkPropertyAnnualSubmissionRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxV2Uk

  def validate: Validated[Seq[MtdError], RetrieveUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(minimumTaxYear, taxYear)
    ).mapN(Def1_RetrieveUkPropertyAnnualSubmissionRequestData)

}
