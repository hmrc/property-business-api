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

package v4.retrieveForeignPropertyAnnualSubmission

import shared.controllers.validators.Validator
import config.AppConfig
import v4.retrieveForeignPropertyAnnualSubmission.def1.Def1_RetrieveForeignPropertyAnnualSubmissionValidator
import v4.retrieveForeignPropertyAnnualSubmission.model.request.RetrieveForeignPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveForeignPropertyAnnualSubmissionValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, businessId: String, taxYear: String): Validator[RetrieveForeignPropertyAnnualSubmissionRequestData] = {
    new Def1_RetrieveForeignPropertyAnnualSubmissionValidator(nino, businessId, taxYear)(appConfig)
  }

}
