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

package v5.createAmendForeignPropertyAnnualSubmission.def1

import cats.data.Validated
import cats.implicits._
import config.PropertyBusinessConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers._
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v5.createAmendForeignPropertyAnnualSubmission.def1.model.request.{
  Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody,
  Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData
}
import v5.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestData

import javax.inject.Inject

class Def1_CreateAmendForeignPropertyAnnualSubmissionValidator @Inject() (nino: String, businessId: String, taxYear: String, body: JsValue)(implicit
    config: PropertyBusinessConfig)
    extends Validator[CreateAmendForeignPropertyAnnualSubmissionRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinimum(TaxYear.fromMtd(config.foreignMinimumTaxYear))

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody]
  private val rulesValidator = new Def1_CreateAmendForeignPropertyAnnualSubmissionRulesValidator()

  def validate: Validated[Seq[MtdError], CreateAmendForeignPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData) andThen rulesValidator.validateBusinessRules

}
