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

package v4.historicFhlUkPropertyPeriodSummary.retrieve

import config.PropertyBusinessConfig
import shared.controllers.validators.Validator
import v4.historicFhlUkPropertyPeriodSummary.retrieve.def1.Def1_RetrieveHistoricFhlUkPeriodSummaryValidator
import v4.historicFhlUkPropertyPeriodSummary.retrieve.model.request.RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveHistoricFhlUkPropertyPeriodSummaryValidatorFactory @Inject() (implicit config: PropertyBusinessConfig) {

  def validator(nino: String, periodId: String): Validator[RetrieveHistoricFhlUkPropertyPeriodSummaryRequestData] =
    new Def1_RetrieveHistoricFhlUkPeriodSummaryValidator(nino, periodId)

}
