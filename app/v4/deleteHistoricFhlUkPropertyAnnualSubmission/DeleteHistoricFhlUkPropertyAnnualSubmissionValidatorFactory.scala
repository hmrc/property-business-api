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

package v4.deleteHistoricFhlUkPropertyAnnualSubmission

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveHistoricTaxYear, ResolveNino}
import api.models.domain.HistoricPropertyType
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.catsSyntaxTuple3Semigroupal
import config.AppConfig
import v4.deleteHistoricFhlUkPropertyAnnualSubmission.model.request.DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class DeleteHistoricFhlUkPropertyAnnualSubmissionValidatorFactory @Inject()(appConfig: AppConfig) {

  private lazy val minimumTaxHistoric = appConfig.minimumTaxYearHistoric
  private lazy val maximumTaxHistoric = appConfig.maximumTaxYearHistoric

  def validator(nino: String, taxYear: String, propertyType: HistoricPropertyType): Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    new Validator[DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] {

      def validate: Validated[Seq[MtdError], DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData] =
        (
          ResolveNino(nino),
          ResolveHistoricTaxYear(minimumTaxHistoric, maximumTaxHistoric, taxYear),
          Valid(propertyType)
        ).mapN(DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData)

    }

}
