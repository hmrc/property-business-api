/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.createAmendUkPropertyAnnualSubmission.def3

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.*
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import play.api.libs.json.JsValue
import v6.createAmendUkPropertyAnnualSubmission.def3.model.request.{
  Def3_CreateAmendUkPropertyAnnualSubmissionRequestBody,
  Def3_CreateAmendUkPropertyAnnualSubmissionRequestData
}
import v6.createAmendUkPropertyAnnualSubmission.model.request.CreateAmendUkPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class Def3_CreateAmendUkPropertyAnnualSubmissionValidator @Inject() (nino: String, businessId: String, taxYear: String, body: JsValue)
    extends Validator[CreateAmendUkPropertyAnnualSubmissionRequestData] {

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def3_CreateAmendUkPropertyAnnualSubmissionRequestBody]()
  private val rulesValidator = new Def3_CreateAmendUkPropertyAnnualSubmissionRulesValidator()

  def validate: Validated[Seq[MtdError], CreateAmendUkPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveJson(body)
    ).mapN((validNino, validBusinessId, validBody) =>
      Def3_CreateAmendUkPropertyAnnualSubmissionRequestData(
        validNino,
        validBusinessId,
        TaxYear.fromMtd(taxYear),
        validBody)) andThen rulesValidator.validateBusinessRules

}
