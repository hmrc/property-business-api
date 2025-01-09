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

package v4.createUkPropertyPeriodSummary.def1

import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveNonEmptyJsonObject}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import common.controllers.validators.resolvers.ResolveTaxYear
import config.AppConfig
import play.api.libs.json.JsValue
import v4.createUkPropertyPeriodSummary.model.request._

import javax.inject.Inject

class Def1_CreateUkPropertyPeriodSummaryValidator @Inject() (nino: String, businessId: String, taxYear: String, body: JsValue)(appConfig: AppConfig)
    extends Validator[CreateUkPropertyPeriodSummaryRequestData] {

  private lazy val minimumTaxYear = appConfig.minimumTaxV2Uk
  private lazy val maximumTaxYear = TaxYear.fromMtd("2023-24")

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def1_CreateUkPropertyPeriodSummaryRequestBody]()
  private val rulesValidator = new Def1_CreateUkPropertyPeriodSummaryRulesValidator()

  def validate: Validated[Seq[MtdError], CreateUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear, minimumTaxYear, maximumTaxYear),
      resolveJson(body)
    ).mapN(Def1_CreateUkPropertyPeriodSummaryRequestData) andThen rulesValidator.validateBusinessRules

}
