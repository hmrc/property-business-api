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

package v4.historicNonFhlUkPropertyPeriodSummary.create

import api.models.utils.JsonErrorValidators
import play.api.libs.json._
import support.UnitSpec
import v4.historicNonFhlUkPropertyPeriodSummary.create.def1.Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryValidator

class CreateHistoricNonFhlUkPropertyPeriodSummaryValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private val validNino   = "AA123456A"
  private val invalidNino = "not-a-nino"

  private val validBody = Json.parse("""
      |{
      | "fromDate": "2017-03-11",
      | "toDate": "2018-03-11",
      |   "income": {
      |     "periodAmount": 123.45,
      |     "premiumsOfLeaseGrant": 2355.45,
      |     "reversePremiums": 454.56,
      |     "otherIncome": 567.89,
      |     "taxDeducted": 234.53,
      |     "rentARoom": {
      |       "rentsReceived": 567.56
      |     }
      |   },
      |  "expenses": {
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34,
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "rentARoom": {
      |      "amountClaimed": 545.9
      |    }
      |  }
      |}
      |""".stripMargin)

  private val invalidBody = Json.parse("""
      |{
      |  "unexpected": 0.0
      |}
      |""".stripMargin)

  private val validatorFactory = new CreateHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory()

  "validator()" should {
    "return the Def1 validator" when {

      "given any valid request" in {
        val result = validatorFactory.validator(validNino, validBody)
        result shouldBe a[Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryValidator]
      }

      "given any invalid request" in {
        val result = validatorFactory.validator(invalidNino, invalidBody)
        result shouldBe a[Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryValidator]
      }
    }

  }

}
