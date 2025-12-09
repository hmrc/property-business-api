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

package v6.createAmendForeignPropertyAnnualSubmission.def3

import cats.data.Validated
import cats.implicits.*
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveNonEmptyJsonObject}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v6.createAmendForeignPropertyAnnualSubmission.def3.model.request
import v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.{
  Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody,
  Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData
}
import v6.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestData

import javax.inject.Singleton

@Singleton
class Def3_CreateAmendForeignPropertyAnnualSubmissionValidator(nino: String, businessId: String, taxYear: String, body: JsValue)
    extends Validator[CreateAmendForeignPropertyAnnualSubmissionRequestData] {

  private val resolveJson    = new ResolveNonEmptyJsonObject[request.Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody]
  private val rulesValidator = new Def3_CreateAmendForeignPropertyAnnualSubmissionRulesValidator()

  def validate: Validated[Seq[MtdError], CreateAmendForeignPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveJson(body)
    ).mapN { (validNino, validBusinessId, validJson) =>
      Def3_CreateAmendForeignPropertyAnnualSubmissionRequestData(
        nino = validNino,
        businessId = validBusinessId,
        taxYear = TaxYear.fromMtd(taxYear),
        body = validJson
      )
    } andThen rulesValidator.validateBusinessRules

}
