/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.controllers.requestParsers.validators

import config.AppConfig

import javax.inject.{Inject, Singleton}
import v2.controllers.requestParsers.validators.validations.{NinoValidation, TaxYearValidation}
import v2.models.errors.MtdError
import v2.models.request.RetrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData

@Singleton
class RetrieveHistoricFhlUkPropertyAnnualSubmissionValidator @Inject()(appConfig: AppConfig) extends
  Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Uk
  private val validationSet   = List(parameterFormatValidation)

  private def parameterFormatValidation: RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData => List[List[MtdError]] =
    (data: RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData) => {
      List(
        NinoValidation.validate(data.nino),
        TaxYearValidation.validate(minTaxYear, data.taxYear),
      )
    }

  override def validate(data: RetrieveHistoricFhlUkPropertyAnnualSubmissionRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
