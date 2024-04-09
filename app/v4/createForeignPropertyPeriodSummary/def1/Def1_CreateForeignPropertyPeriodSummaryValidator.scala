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

package v4.createForeignPropertyPeriodSummary.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYear}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v4.createForeignPropertyPeriodSummary.def1.Def1_CreateForeignPropertyPeriodSummaryRulesValidator.validateBusinessRules
import v4.createForeignPropertyPeriodSummary.def1.model.request.Def1_CreateForeignPropertyPeriodSummaryRequestBody
import v4.createForeignPropertyPeriodSummary.model.request.{CreateForeignPropertyPeriodSummaryRequestData, Def1_CreateForeignPropertyPeriodSummaryRequestData}

class Def1_CreateForeignPropertyPeriodSummaryValidator(nino: String, businessId: String, taxYear: String, body: JsValue, appConfig: AppConfig)
    extends Validator[CreateForeignPropertyPeriodSummaryRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxV2Foreign

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateForeignPropertyPeriodSummaryRequestBody]()

  def validate: Validated[Seq[MtdError], CreateForeignPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(minimumTaxYear, taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateForeignPropertyPeriodSummaryRequestData) andThen validateBusinessRules

}
