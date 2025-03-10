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

package v6.historicFhlUkPropertyPeriodSummary.create

import config.MockPropertyBusinessConfig
import play.api.libs.json.Json
import shared.controllers.validators.Validator
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v6.historicFhlUkPropertyPeriodSummary.create.def1.Def1_CreateHistoricFhlUkPropertyPeriodSummaryValidator
import v6.historicFhlUkPropertyPeriodSummary.create.model.request.CreateHistoricFhlUkPropertyPeriodSummaryRequestData

class CreateHistoricFhlUkPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig with JsonErrorValidators {

  private val validNino   = "AA123456A"
  private val invalidNino = "not-a-nino"

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

  private val invalidRequestBody = Json.parse(
    """
      | {
      |   "unexpected": ""
      | }
      |""".stripMargin
  )

  private val validatorFactory = new CreateHistoricFhlUkPropertyPeriodSummaryValidatorFactory

  "validator()" when {
    "given any valid request" should {
      "return the Def1 Validator" in new SetupConfig {
        val result: Validator[CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validatorFactory.validator(validNino, validRequestBody)

        result shouldBe a[Def1_CreateHistoricFhlUkPropertyPeriodSummaryValidator]
      }
    }

    "given any invalid request" should {
      "return the Def1 Validator" in new SetupConfig {
        val result: Validator[CreateHistoricFhlUkPropertyPeriodSummaryRequestData] =
          validatorFactory.validator(invalidNino, invalidRequestBody)

        result shouldBe a[Def1_CreateHistoricFhlUkPropertyPeriodSummaryValidator]
      }
    }
  }

}
