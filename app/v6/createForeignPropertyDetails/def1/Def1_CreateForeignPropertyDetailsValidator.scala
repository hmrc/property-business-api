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

package v6.createForeignPropertyDetails.def1

import cats.data.Validated
import cats.implicits.*
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.*
import shared.models.errors.MtdError
import v6.createForeignPropertyDetails.def1.Def1_CreateForeignPropertyDetailsRulesValidator.validateBusinessRules
import v6.createForeignPropertyDetails.def1.model.request.{Def1_CreateForeignPropertyDetailsRequestBody, Def1_CreateForeignPropertyDetailsRequestData}
import v6.createForeignPropertyDetails.model.request.CreateForeignPropertyDetailsRequestData

class Def1_CreateForeignPropertyDetailsValidator(nino: String, businessId: String, taxYear: String, body: JsValue)
    extends Validator[CreateForeignPropertyDetailsRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_CreateForeignPropertyDetailsRequestBody]()

  def validate: Validated[Seq[MtdError], CreateForeignPropertyDetailsRequestData] =
    (
      ResolveNino(nino),
      ResolveBusinessId(businessId),
      ResolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN((validNino, validBusinessId, validTaxYear, validBody) =>
      Def1_CreateForeignPropertyDetailsRequestData(validNino, validBusinessId, validTaxYear, validBody)) andThen validateBusinessRules

}
