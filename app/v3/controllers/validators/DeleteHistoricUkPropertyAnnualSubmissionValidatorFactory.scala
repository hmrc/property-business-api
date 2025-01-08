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
import cats.data.Validated.Valid
import cats.implicits.catsSyntaxTuple3Semigroupal
import common.controllers.validators.resolvers.ResolveHistoricTaxYear
import common.models.domain.HistoricPropertyType
import config.AppConfig
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.ResolveNino
import shared.models.errors.MtdError
import v3.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class DeleteHistoricUkPropertyAnnualSubmissionValidatorFactory @Inject() (appConfig: AppConfig) {

  private lazy val minimumTaxHistoric = appConfig.minimumTaxYearHistoric
  private lazy val maximumTaxHistoric = appConfig.maximumTaxYearHistoric

  def validator(nino: String, taxYear: String, propertyType: HistoricPropertyType): Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData] =
    new Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData] {

      def validate: Validated[Seq[MtdError], DeleteHistoricUkPropertyAnnualSubmissionRequestData] =
        (
          ResolveNino(nino),
          ResolveHistoricTaxYear(minimumTaxHistoric, maximumTaxHistoric, taxYear),
          Valid(propertyType)
        ).mapN(DeleteHistoricUkPropertyAnnualSubmissionRequestData)

    }

}
