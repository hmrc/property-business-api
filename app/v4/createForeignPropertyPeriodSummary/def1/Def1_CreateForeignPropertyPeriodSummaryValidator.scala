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

package v4.createForeignPropertyPeriodSummary.def1

import cats.data.Validated
import cats.implicits.*
import config.PropertyBusinessConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.*
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v4.createForeignPropertyPeriodSummary.def1.Def1_CreateForeignPropertyPeriodSummaryRulesValidator.validateBusinessRules
import v4.createForeignPropertyPeriodSummary.model.request.*

import javax.inject.Inject

class Def1_CreateForeignPropertyPeriodSummaryValidator @Inject() (nino: String,
                                                                  businessId: String,
                                                                  taxYear: String,
                                                                  maxTaxYear: TaxYear,
                                                                  body: JsValue)(implicit config: PropertyBusinessConfig)
    extends Validator[CreateForeignPropertyPeriodSummaryRequestData] {

  private val resolveTaxYear = ResolveTaxYearMinMax((TaxYear.fromMtd(config.foreignMinimumTaxYear), maxTaxYear))

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateForeignPropertyPeriodSummaryRequestBody]()

  def validate: Validated[Seq[MtdError], CreateForeignPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def1_CreateForeignPropertyPeriodSummaryRequestData.apply) andThen validateBusinessRules

}
