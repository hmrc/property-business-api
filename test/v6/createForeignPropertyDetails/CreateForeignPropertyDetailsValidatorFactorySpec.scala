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

package v6.createForeignPropertyDetails

import play.api.libs.json.{JsObject, Json}
import shared.controllers.validators.Validator
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createForeignPropertyDetails.def1.Def1_CreateForeignPropertyDetailsValidator
import v6.createForeignPropertyDetails.def1.model.Def1_CreateForeignPropertyDetailsFixtures
import v6.createForeignPropertyDetails.model.request.CreateForeignPropertyDetailsRequestData

class CreateForeignPropertyDetailsValidatorFactorySpec extends UnitSpec with JsonErrorValidators with Def1_CreateForeignPropertyDetailsFixtures {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2026-27"

  private val validatorFactory = new CreateForeignPropertyDetailsValidatorFactory

  "validator()" when {
    "given a valid tax year" should {
      "return the Validator for schema definition 1" in {

        val requestBody: JsObject = Json.obj(
          "propertyName" -> "Bob & Bobby Co",
          "countryCode"  -> "FRA",
          "endDate"      -> "2026-08-24",
          "endReason"    -> "no-longer-renting-property-out"
        )
        val result: Validator[CreateForeignPropertyDetailsRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear, requestBody)

        result shouldBe a[Def1_CreateForeignPropertyDetailsValidator]
      }
    }
  }

}
