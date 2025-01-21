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

package v5.createAmendHistoricFhlUkPropertyAnnualSubmission

import play.api.libs.json.Json
import shared.controllers.validators.Validator
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v5.createAmendHistoricFhlUkPropertyAnnualSubmission.def1.Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator
import v5.createAmendHistoricFhlUkPropertyAnnualSubmission.model.request._

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2019-20"

  private val validRequestBody = Json.parse(
    """
      |  {
      |     "annualAdjustments": {
      |        "lossBroughtForward": 111.50,
      |        "privateUseAdjustment": 222.00,
      |        "balancingCharge": 333.00,
      |        "periodOfGraceAdjustment": true,
      |        "businessPremisesRenovationAllowanceBalancingCharges": 444.00,
      |        "nonResidentLandlord": false,
      |        "rentARoom": {
      |           "jointlyLet": true
      |        }
      |     },
      |     "annualAllowances": {
      |        "annualInvestmentAllowance": 111.00,
      |        "businessPremisesRenovationAllowance": 222.00,
      |        "otherCapitalAllowance": 333.00
      |     }
      |  }
      |""".stripMargin
  )

  private val validatorFactory = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidatorFactory

  "validator()" when {
    "given any tax year" should {
      "return the Validator for schema definition 1" in {
        val requestBody = validRequestBody
        val result: Validator[CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validTaxYear, requestBody)
        result shouldBe a[Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionValidator]
      }
    }
  }

}
