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

package v6.updateForeignPropertyDetails

import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import v6.updateForeignPropertyDetails.def1.Def1_UpdateForeignPropertyDetailsValidator
import v6.updateForeignPropertyDetails.model.request.UpdateForeignPropertyDetailsRequestData

import javax.inject.Singleton

@Singleton
class UpdateForeignPropertyDetailsValidatorFactory {

  def validator(nino: String, propertyId: String, taxYear: String, body: JsValue): Validator[UpdateForeignPropertyDetailsRequestData] = {
    val schema = UpdateForeignPropertyDetailsSchema.schemaFor(taxYear)

    schema match {
      case Valid(_)        => new Def1_UpdateForeignPropertyDetailsValidator(nino, propertyId, taxYear, body)
      case Invalid(errors) => Validator.returningErrors(errors)
    }

  }

}
