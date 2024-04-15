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

package v4.controllers.createUkPropertyPeriodSummary.def2

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveNonEmptyJsonObject, ResolveTaxYear}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple4Semigroupal
import play.api.libs.json.JsValue
import v4.controllers.createUkPropertyPeriodSummary.model.request._

import javax.inject.Inject

class Def2_CreateUkPropertyPeriodSummaryValidator @Inject()(nino: String, businessId: String, taxYear: String, body: JsValue)
    extends Validator[CreateUkPropertyPeriodSummaryRequestData] {

  private val resolveJson    = new ResolveNonEmptyJsonObject[Def2_CreateUkPropertyPeriodSummaryRequestBody]()
  private val rulesValidator = new Def2_CreateUkPropertyPeriodSummaryRulesValidator()

  def validate: Validated[Seq[MtdError], CreateUkPropertyPeriodSummaryRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(Def2_CreateUkPropertyPeriodSummaryRequestData) andThen rulesValidator.validateBusinessRules

}
