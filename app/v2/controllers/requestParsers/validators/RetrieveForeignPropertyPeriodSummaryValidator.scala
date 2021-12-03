/*
 * Copyright 2021 HM Revenue & Customs
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
import v2.controllers.requestParsers.validators.validations.{BusinessIdValidation, NinoValidation, SubmissionIdValidation, TaxYearValidation}
import v2.models.errors.MtdError
import v2.models.request.retrieveForeignPropertyPeriodSummary.RetrieveForeignPropertyPeriodSummaryRawData
import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveForeignPropertyPeriodSummaryValidator @Inject()(appConfig: AppConfig) extends Validator[RetrieveForeignPropertyPeriodSummaryRawData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Foreign
  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: RetrieveForeignPropertyPeriodSummaryRawData => List[List[MtdError]] =
    (data: RetrieveForeignPropertyPeriodSummaryRawData) => {
      List(
        NinoValidation.validate(data.nino),
        BusinessIdValidation.validate(data.businessId),
        TaxYearValidation.validate(minTaxYear, data.taxYear),
        SubmissionIdValidation.validate(data.submissionId)
      )
    }

  override def validate(data: RetrieveForeignPropertyPeriodSummaryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}