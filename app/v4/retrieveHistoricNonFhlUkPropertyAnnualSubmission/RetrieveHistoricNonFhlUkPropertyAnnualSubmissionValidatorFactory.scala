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

package v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission

import shared.controllers.validators.Validator
import config.AppConfig
import v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission.def1.Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidator
import v4.retrieveHistoricNonFhlUkPropertyAnnualSubmission.model.request.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory @Inject() (implicit appConfig: AppConfig) {

  def validator(nino: String, taxYear: String): Validator[RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData] =
    new Def1_RetrieveHistoricNonFhlUkPropertyAnnualSubmissionValidator(nino, taxYear)

}
