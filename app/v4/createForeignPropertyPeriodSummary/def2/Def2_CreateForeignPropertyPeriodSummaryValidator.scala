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

package v4.createForeignPropertyPeriodSummary.def2

import cats.data.Validated
import cats.implicits._
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYearMaximum}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v4.createForeignPropertyPeriodSummary.def2.Def2_CreateForeignPropertyPeriodSummaryRulesValidator.validateBusinessRules
import v4.createForeignPropertyPeriodSummary.model.request._

import javax.inject.Inject

class Def2_CreateForeignPropertyPeriodSummaryValidator @Inject() (nino: String,
                                                                  businessId: String,
                                                                  taxYear: String,
                                                                  maxTaxYear: TaxYear,
                                                                  body: JsValue)
    extends Validator[CreateForeignPropertyPeriodSummaryRequestData] {

  private val resolveTaxYear = ResolveTaxYearMaximum(maxTaxYear)

  private val resolveJson = new ResolveNonEmptyJsonObject[Def2_CreateForeignPropertyPeriodSummaryRequestBody]()

  def validate: Validated[Seq[MtdError], CreateForeignPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def2_CreateForeignPropertyPeriodSummaryRequestData) andThen validateBusinessRules

}
