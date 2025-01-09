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

package v4.historicFhlUkPropertyPeriodSummary.amend

import shared.controllers.validators.Validator
import config.AppConfig
import play.api.libs.json.JsValue
import v4.historicFhlUkPropertyPeriodSummary.amend.def1.Def1_AmendHistoricFhlUkPropertyPeriodSummaryValidator
import v4.historicFhlUkPropertyPeriodSummary.amend.request.AmendHistoricFhlUkPropertyPeriodSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class AmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, periodId: String, body: JsValue): Validator[AmendHistoricFhlUkPropertyPeriodSummaryRequestData] =
    new Def1_AmendHistoricFhlUkPropertyPeriodSummaryValidator(nino, periodId, body, appConfig)

}
