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

package v4.amendForeignPropertyPeriodSummary.def1

import cats.data.Validated
import cats.implicits._
import common.controllers.validators.resolvers.{ResolveSubmissionId, ResolveTaxYear}
import config.AppConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers._
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v4.amendForeignPropertyPeriodSummary.def1.Def1_AmendForeignPropertyPeriodSummaryRulesValidator.validateBusinessRules
import v4.amendForeignPropertyPeriodSummary.model.request.{
  AmendForeignPropertyPeriodSummaryRequestData,
  Def1_AmendForeignPropertyPeriodSummaryRequestBody,
  Def1_AmendForeignPropertyPeriodSummaryRequestData
}

class Def1_AmendForeignPropertyPeriodSummaryValidator(nino: String,
                                                      businessId: String,
                                                      taxYear: String,
                                                      maxTaxYear: TaxYear,
                                                      submissionId: String,
                                                      body: JsValue,
                                                      appConfig: AppConfig)
    extends Validator[AmendForeignPropertyPeriodSummaryRequestData] {

  private lazy val minTaxYear = appConfig.minimumTaxV2Foreign

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_AmendForeignPropertyPeriodSummaryRequestBody]()

  def validate: Validated[Seq[MtdError], AmendForeignPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear, minTaxYear, maxTaxYear),
      ResolveSubmissionId(submissionId),
      resolveJson(body)
    ).mapN(Def1_AmendForeignPropertyPeriodSummaryRequestData) andThen validateBusinessRules

}
