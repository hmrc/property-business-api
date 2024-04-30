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

package v4.amendHistoricNonFhlUkPropertyPeriodSummary

import api.controllers.validators.Validator
import config.AppConfig
import play.api.libs.json.JsValue
import v4.amendHistoricNonFhlUkPropertyPeriodSummary.def1.Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryValidator
import v4.amendHistoricNonFhlUkPropertyPeriodSummary.model.request.AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class AmendHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory @Inject() (implicit appConfig: AppConfig) {

  def validator(nino: String, periodId: String, body: JsValue): Validator[AmendHistoricNonFhlUkPropertyPeriodSummaryRequestData] =
    new Def1_AmendHistoricNonFhlUkPropertyPeriodSummaryValidator(nino, periodId, body)

}
