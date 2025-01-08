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

package v3.controllers.validators

import cats.data.Validated
import cats.implicits._
import config.AppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYear}
import shared.models.errors.MtdError
import v3.models.request.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveUkPropertyAnnualSubmissionValidatorFactory @Inject() (appConfig: AppConfig) {

  private lazy val minimumTaxYear = appConfig.minimumTaxV2Uk

  def validator(nino: String, businessId: String, taxYear: String): Validator[RetrieveUkPropertyAnnualSubmissionRequestData] =
    new Validator[RetrieveUkPropertyAnnualSubmissionRequestData] {

      def validate: Validated[Seq[MtdError], RetrieveUkPropertyAnnualSubmissionRequestData] =
        (
          ResolveNino(nino),
          ResolveBusinessId(businessId),
          ResolveTaxYear(minimumTaxYear, taxYear)
        ).mapN(RetrieveUkPropertyAnnualSubmissionRequestData)

    }

}
