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

package v4.createForeignPropertyPeriodSummary

import api.controllers.validators.Validator
import config.AppConfig
import play.api.libs.json.JsValue
import v4.createForeignPropertyPeriodSummary.def1.Def1_CreateForeignPropertyPeriodSummaryValidator
import v4.createForeignPropertyPeriodSummary.def2.Def2_CreateValidator
import v4.createForeignPropertyPeriodSummary.model.request.CreateForeignPropertyPeriodSummaryRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class CreateForeignPropertyPeriodSummaryValidatorFactory @Inject() (appConfig: AppConfig) {

  def validator(nino: String, businessId: String, taxYear: String, body: JsValue): Validator[CreateForeignPropertyPeriodSummaryRequestData] =
    if (taxYear >= "2024-25") {
      new Def2_CreateValidator(nino, businessId, taxYear, body)
    } else {
      new Def1_CreateForeignPropertyPeriodSummaryValidator(nino, businessId, taxYear, body, appConfig)

    }

}
