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

package v6.updateForeignPropertyDetails.def1

import cats.data.Validated
import cats.implicits.*
import common.controllers.validators.resolvers.ResolveUuid
import common.models.errors.PropertyIdFormatError
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.*
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v6.updateForeignPropertyDetails.def1.Def1_UpdateForeignPropertyDetailsRulesValidator.validateBusinessRules
import v6.updateForeignPropertyDetails.def1.model.request.{Def1_UpdateForeignPropertyDetailsRequestBody, Def1_UpdateForeignPropertyDetailsRequestData}
import v6.updateForeignPropertyDetails.model.request.UpdateForeignPropertyDetailsRequestData
import common.models.domain.PropertyId

import javax.inject.Inject

class Def1_UpdateForeignPropertyDetailsValidator @Inject() (nino: String, propertyId: String, taxYear: String, body: JsValue)
    extends Validator[UpdateForeignPropertyDetailsRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[Def1_UpdateForeignPropertyDetailsRequestBody]()

  def validate: Validated[Seq[MtdError], UpdateForeignPropertyDetailsRequestData] =
    (
      ResolveNino(nino),
      ResolveUuid(propertyId, PropertyIdFormatError)(PropertyId.apply),
      resolveJson(body)
    ).mapN((validNino, validPropertyId, validBody) =>
      Def1_UpdateForeignPropertyDetailsRequestData(validNino, validPropertyId, TaxYear.fromMtd(taxYear), validBody)) andThen validateBusinessRules

}
