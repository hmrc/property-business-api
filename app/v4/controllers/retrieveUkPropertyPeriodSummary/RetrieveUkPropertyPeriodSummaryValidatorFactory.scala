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

package v4.controllers.retrieveUkPropertyPeriodSummary

import api.controllers.validators.Validator
import config.AppConfig
import v4.controllers.retrieveUkPropertyPeriodSummary.def1.Def1_RetrieveUkPropertyPeriodSummaryValidator
import v4.controllers.retrieveUkPropertyPeriodSummary.def2.Def2_RetrieveUkPropertyPeriodSummaryValidator
import v4.controllers.retrieveUkPropertyPeriodSummary.model.request.RetrieveUkPropertyPeriodSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveUkPropertyPeriodSummaryValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, businessId: String, taxYear: String, submissionId: String): Validator[RetrieveUkPropertyPeriodSummaryRequestData] = {
    taxYear match {
      case "2024-25" => new Def2_RetrieveUkPropertyPeriodSummaryValidator (nino, businessId, taxYear, submissionId) (appConfig)
      case _ => new Def1_RetrieveUkPropertyPeriodSummaryValidator (nino, businessId, taxYear, submissionId) (appConfig)
    }
  }

}
