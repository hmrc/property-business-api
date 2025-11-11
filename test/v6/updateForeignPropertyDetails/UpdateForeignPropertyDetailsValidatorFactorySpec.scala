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

import play.api.libs.json.{JsObject, Json}
import shared.controllers.validators.Validator
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.updateForeignPropertyDetails.def1.Def1_UpdateForeignPropertyDetailsValidator
import v6.updateForeignPropertyDetails.model.request.UpdateForeignPropertyDetailsRequestData

class UpdateForeignPropertyDetailsValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private val validNino       = "AA999999A"
  private val validPropertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb"
  private val validTaxYear    = "2026-27"

  private val validatorFactory = new UpdateForeignPropertyDetailsValidatorFactory

  "UpdateForeignPropertyDetailsValidatorFactory" when {
    "given a valid tax year" should {
      "return the Validator for schema definition 1" in {

        val requestBody: JsObject = Json.obj(
          "propertyName" -> "Bob & Bobby Co",
          "endDate"      -> "2026-08-24",
          "endReason"    -> "no-longer-renting-property-out"
        )

        val result: Validator[UpdateForeignPropertyDetailsRequestData] =
          validatorFactory.validator(validNino, validPropertyId, validTaxYear, requestBody)

        result shouldBe a[Def1_UpdateForeignPropertyDetailsValidator]
      }
    }
  }

}
