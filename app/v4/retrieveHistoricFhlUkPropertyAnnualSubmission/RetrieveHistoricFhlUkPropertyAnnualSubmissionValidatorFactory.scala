/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.retrieveHistoricFhlUkPropertyAnnualSubmission

import config.PropertyBusinessConfig
import shared.controllers.validators.Validator
import v4.retrieveHistoricFhlUkPropertyAnnualSubmission.def1.Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionValidator
import v4.retrieveHistoricFhlUkPropertyAnnualSubmission.model.request.RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveHistoricFhlUkPropertyAnnualSubmissionValidatorFactory @Inject() (implicit config: PropertyBusinessConfig) {

  def validator(nino: String, taxYear: String): Validator[RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData] =
    new Def1_RetrieveHistoricFhlUkPropertyAnnualSubmissionValidator(nino, taxYear)

}
