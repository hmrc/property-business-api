/*
 * Copyright 2024 HM Revenue & Customs
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

package v5.createAmendUkPropertyCumulativeSummary

import config.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import shared.controllers.validators.AlwaysErrorsValidator
import shared.utils.UnitSpec
import v5.createAmendUkPropertyCumulativeSummary.def1.Def1_CreateAmendUkPropertyCumulativeSummaryValidator

class CreateAmendUkPropertyCumulativeSummaryValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private val consolidatedRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2023-04-01",
      |  "toDate": "2024-04-01",
      |  "ukProperty": {
      |    "income": {
      |      "premiumsOfLeaseGrant": 42.12,
      |      "reversePremiums": 84.31,
      |      "periodAmount": 9884.93,
      |      "taxDeducted": 842.99,
      |      "otherIncome": 31.44,
      |      "rentARoom": {
      |        "rentsReceived": 947.66
      |      }
      |    },
      |    "expenses": {
      |      "residentialFinancialCost": 9000.10,
      |      "residentialFinancialCostsCarriedForward": 300.13,
      |      "rentARoom": {
      |        "amountClaimed": 860.88
      |      },
      |      "consolidatedExpenses": -988.18
      |    }
      |  }
      |}
      """.stripMargin
  )

  private def validatorFor(taxYear: String) =
    new CreateAmendUkPropertyCumulativeSummaryValidatorFactory().validator(
      nino = "ignoredNino",
      businessId = "ignored",
      taxYear = taxYear,
      body = consolidatedRequestBodyJson)

  "CreateAmendUkPropertyCumulativeSummaryValidatorFactory" when {
    "given a request corresponding to a Def1 schema" should {
      "return a Def1 validator" in {
        validatorFor("2025-26") shouldBe a[Def1_CreateAmendUkPropertyCumulativeSummaryValidator]
      }
    }

    "given a request where no valid schema could be determined" should {
      "return a validator returning the errors" in {
        validatorFor("BAD_TAX_YEAR") shouldBe an[AlwaysErrorsValidator]
      }
    }
  }

}
