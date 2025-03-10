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

package v6.createAmendForeignPropertyCumulativePeriodSummary

import play.api.libs.json._
import shared.controllers.validators.Validator
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryValidator
import v6.createAmendForeignPropertyCumulativePeriodSummary.model.request.CreateAmendForeignPropertyCumulativePeriodSummaryRequestData

class CreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2025-26"

  private def validBody() = Json.parse(s"""
       |{
       |   "foreignProperty": {
       |      "countryCode":"AFG"
       |   }
       |}
       |""".stripMargin)

  private val validatorFactory = new CreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory

  "validator()" when {
    "given a valid tax year" should {
      "return the Validator for schema definition 1" in {

        val requestBody = validBody()
        val result: Validator[CreateAmendForeignPropertyCumulativePeriodSummaryRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear, requestBody)

        result shouldBe a[Def1_CreateAmendForeignPropertyCumulativePeriodSummaryValidator]
      }
    }
  }

}
