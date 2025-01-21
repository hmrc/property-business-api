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

package v5.amendForeignPropertyPeriodSummary.def1

import cats.data.Validated
import cats.implicits._
import common.controllers.validators.resolvers.ResolveSubmissionId
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers._
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v5.amendForeignPropertyPeriodSummary.def1.Def1_AmendForeignPropertyPeriodSummaryRulesValidator.validateBusinessRules
import v5.amendForeignPropertyPeriodSummary.model.request.{
  AmendForeignPropertyPeriodSummaryRequestData,
  Def1_AmendForeignPropertyPeriodSummaryRequestBody,
  Def1_AmendForeignPropertyPeriodSummaryRequestData
}

class Def1_AmendForeignPropertyPeriodSummaryValidator(nino: String,
                                                      businessId: String,
                                                      taxYear: String,
                                                      maxTaxYear: TaxYear,
                                                      submissionId: String,
                                                      body: JsValue)
    extends Validator[AmendForeignPropertyPeriodSummaryRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinMax((TaxYear.fromMtd("2021-22"), maxTaxYear))

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_AmendForeignPropertyPeriodSummaryRequestBody]()

  def validate: Validated[Seq[MtdError], AmendForeignPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear),
      ResolveSubmissionId(submissionId),
      resolveJson(body)
    ).mapN(Def1_AmendForeignPropertyPeriodSummaryRequestData) andThen validateBusinessRules

}
