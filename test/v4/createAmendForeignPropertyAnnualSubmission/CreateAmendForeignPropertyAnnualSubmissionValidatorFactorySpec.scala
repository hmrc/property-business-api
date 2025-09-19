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

package v4.createAmendForeignPropertyAnnualSubmission

import config.MockPropertyBusinessConfig
import play.api.libs.json.*
import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v4.createAmendForeignPropertyAnnualSubmission.def1.Def1_CreateAmendForeignPropertyAnnualSubmissionValidator
import v4.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestData

class CreateAmendForeignPropertyAnnualSubmissionValidatorFactorySpec extends UnitSpec with MockPropertyBusinessConfig {

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2023-24"

  private val validStructuredBuildingAllowance =
    Json.parse("""{
        |  "amount": 3545.12,
        |  "firstYear": {
        |    "qualifyingDate": "2020-03-29",
        |    "qualifyingAmountExpenditure": 3453.34
        |  },
        |  "building": {
        |    "name": "Building name",
        |    "number": "12",
        |    "postcode": "TF3 4GH"
        |  }
        |}""".stripMargin)

  private def entryWith(countryCode: String, structuredBuildingAllowance: JsValue*) =
    Json.parse(s"""
                  |    {
                  |      "countryCode": "$countryCode",
                  |      "adjustments": {
                  |        "privateUseAdjustment": 4553.34,
                  |        "balancingCharge": 3453.34
                  |      },
                  |      "allowances": {
                  |        "annualInvestmentAllowance": 38330.95,
                  |        "costOfReplacingDomesticItems": 41985.17,
                  |        "zeroEmissionsGoodsVehicleAllowance": 9769.19,
                  |        "otherCapitalAllowance": 1049.21,
                  |        "electricChargePointAllowance": 3565.45,
                  |        "structuredBuildingAllowance": ${JsArray(structuredBuildingAllowance)},
                  |        "zeroEmissionsCarAllowance": 3456.34
                  |      }
                  |    }""".stripMargin)

  private def bodyWith(nonFhlEntries: JsValue*) = Json.parse(
    s"""{
       |  "foreignFhlEea": {
       |    "adjustments": {
       |      "privateUseAdjustment": 34343.45,
       |      "balancingCharge": 53543.23,
       |      "periodOfGraceAdjustment": true
       |    },
       |    "allowances": {
       |      "annualInvestmentAllowance": 3434.23,
       |      "otherCapitalAllowance": 1343.34,
       |      "electricChargePointAllowance": 6565.45,
       |      "zeroEmissionsCarAllowance": 3456.34
       |    }
       |  },
       |  "foreignNonFhlProperty": ${JsArray(nonFhlEntries)}
       |}
       |""".stripMargin
  )

  private val validBody = bodyWith(entryWith("AFG", validStructuredBuildingAllowance))

  private val validatorFactory = new CreateAmendForeignPropertyAnnualSubmissionValidatorFactory

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in new SetupConfig {
        val result: Validator[CreateAmendForeignPropertyAnnualSubmissionRequestData] =
          validatorFactory.validator(validNino, validBusinessId, validTaxYear, validBody)

        result shouldBe a[Def1_CreateAmendForeignPropertyAnnualSubmissionValidator]
      }

    }
  }

}
