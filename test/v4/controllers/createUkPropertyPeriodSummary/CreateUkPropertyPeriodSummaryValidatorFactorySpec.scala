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

package v4.controllers.createUkPropertyPeriodSummary

import api.controllers.validators.Validator
import api.models.domain.TaxYear
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v4.controllers.createUkPropertyPeriodSummary.def1.Def1_CreateUkPropertyPeriodSummaryValidator
import v4.controllers.createUkPropertyPeriodSummary.model.request.CreateUkPropertyPeriodSummaryRequestData

class CreateUkPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with MockAppConfig with JsonErrorValidators {

  private val validNino       = "AA123456A"
  private val validTaxYear    = "2023-24"
  private val validTysTaxYear = "2023-24"
  private val validBusinessId = "XAIS12345678901"

  private val validBody = Json.parse("""
      |{
      |    "fromDate": "2020-01-01",
      |    "toDate": "2020-01-31",
      |    "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99,
      |            "taxDeducted": 3123.21,
      |            "rentARoom": {
      |                "rentsReceived": 532.12
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "travelCosts": 974.47,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            }
      |        }
      |    },
      |    "ukNonFhlProperty": {
      |        "income": {
      |            "premiumsOfLeaseGrant": 42.12,
      |            "reversePremiums": 84.31,
      |            "periodAmount": 9884.93,
      |            "taxDeducted": 842.99,
      |            "otherIncome": 31.44,
      |            "rentARoom": {
      |                "rentsReceived": 947.66
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "residentialFinancialCost": 12.34,
      |            "travelCosts": 974.47,
      |            "residentialFinancialCostsCarriedForward": 12.34,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            }
      |        }
      |    }
      |}
      |""".stripMargin)

  private val validatorFactory   = new CreateUkPropertyPeriodSummaryValidatorFactory(mockAppConfig)
  private def setupMocks(): Unit = MockedAppConfig.minimumTaxV2Uk.returns(TaxYear.starting(2022)).anyNumberOfTimes()

  "validator" when {
    "given a valid request" should {
      "return the Validator for schema definition 1" in {
        setupMocks()
        val result: Validator[CreateUkPropertyPeriodSummaryRequestData] =
          validatorFactory.validator(validNino, validTysTaxYear, validBusinessId, validBody)

        result shouldBe a[Def1_CreateUkPropertyPeriodSummaryValidator]
      }

      "passed the minimum supported taxYear" in {
        setupMocks()
        val result: Validator[CreateUkPropertyPeriodSummaryRequestData] =
          validatorFactory.validator(validNino, validTaxYear, validBusinessId, validBody)
        result shouldBe a[Def1_CreateUkPropertyPeriodSummaryValidator]
      }
    }

  }

}
