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

package v2.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveHistoricTaxYear, ResolveNino}
import api.models.domain.HistoricPropertyType
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.catsSyntaxTuple3Semigroupal
import config.AppConfig
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class DeleteHistoricUkPropertyAnnualSubmissionValidatorFactory @Inject() (appConfig: AppConfig) {

  private val minimumTaxHistoric = appConfig.minimumTaxHistoric + 1
  private val maximumTaxHistoric = appConfig.maximumTaxHistoric

  def validator(nino: String, taxYear: String, propertyType: HistoricPropertyType): Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData] =
    new Validator[DeleteHistoricUkPropertyAnnualSubmissionRequestData] {

      def validate: Validated[Seq[MtdError], DeleteHistoricUkPropertyAnnualSubmissionRequestData] =
        (
          ResolveNino(nino),
          ResolveHistoricTaxYear(minimumTaxHistoric, maximumTaxHistoric, taxYear, None, None),
          Valid(propertyType)
        ).mapN(DeleteHistoricUkPropertyAnnualSubmissionRequestData)

    }

}
