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

package v4.createAmendForeignPropertyAnnualSubmission.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYear}
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import play.api.libs.json.JsValue
import v4.createAmendForeignPropertyAnnualSubmission.model.request._

import javax.inject.{Inject, Singleton}

@Singleton
class Def1_CreateAmendForeignPropertyAnnualSubmissionValidator @Inject() (nino: String, businessId: String, taxYear: String, body: JsValue)(
    appConfig: AppConfig)
    extends Validator[CreateAmendForeignPropertyAnnualSubmissionRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxV2Foreign
  private lazy val maximumTaxYear = TaxYear.fromMtd("2024-25")

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody]
  private val rulesValidator = new Def1_CreateAmendForeignPropertyAnnualSubmissionRulesValidator()

  def validate: Validated[Seq[MtdError], CreateAmendForeignPropertyAnnualSubmissionRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear, minimumTaxYear, maximumTaxYear),
      resolveJson(body)
    ).mapN(Def1_CreateAmendForeignPropertyAnnualSubmissionRequestData) andThen rulesValidator.validateBusinessRules

}
