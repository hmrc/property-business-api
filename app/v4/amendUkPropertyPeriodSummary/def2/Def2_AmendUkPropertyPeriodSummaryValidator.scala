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

package v4.amendUkPropertyPeriodSummary.def2

import api.controllers.validators.Validator
import api.controllers.validators.resolvers._
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits.catsSyntaxTuple5Semigroupal
import play.api.libs.json.JsValue
import v4.amendUkPropertyPeriodSummary.model.request._

import javax.inject.Inject

class Def2_AmendUkPropertyPeriodSummaryValidator @Inject() (nino: String, businessId: String, taxYear: String, submissionId: String, body: JsValue)
    extends Validator[AmendUkPropertyPeriodSummaryRequestData] {

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def2_AmendUkPropertyPeriodSummaryRequestBody]()
  private val rulesValidator = new Def2_AmendUkPropertyPeriodSummaryRulesValidator()

  def validate: Validated[Seq[MtdError], AmendUkPropertyPeriodSummaryRequestData] = {

    val result: Validated[Seq[MtdError], Def2_AmendUkPropertyPeriodSummaryRequestData] = (
      ResolveNino(nino),
      ResolveTaxYear(taxYear),
      ResolveBusinessId(businessId),
      ResolveSubmissionId(submissionId),
      resolveJson(body)
    ).mapN(Def2_AmendUkPropertyPeriodSummaryRequestData) andThen rulesValidator.validateBusinessRules

    result.fold(e => Invalid(e), e => Valid(e.toSubmission))
  }

}
