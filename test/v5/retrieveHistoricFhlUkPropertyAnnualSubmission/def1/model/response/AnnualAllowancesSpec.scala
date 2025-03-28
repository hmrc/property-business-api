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

package v5.retrieveHistoricFhlUkPropertyAnnualSubmission.def1.model.response

import play.api.libs.json.Json
import shared.utils.UnitSpec

class AnnualAllowancesSpec extends UnitSpec {

  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  private val annualAllowances =
    AnnualAllowances(decimal("200.00"), decimal("300.00"), decimal("400.02"), decimal("10.02"))

  private val writesJson = Json.parse(
    """
      |{
      |   "annualInvestmentAllowance": 200.00,
      |   "businessPremisesRenovationAllowance": 300.00,
      |   "otherCapitalAllowance": 400.02,
      |   "propertyIncomeAllowance": 10.02
      |}
      |""".stripMargin
  )

  private val readsJson = Json.parse("""
     |{
     |    "annualInvestmentAllowance": 200.00,
     |    "businessPremisesRenovationAllowance": 300.00,
     |    "otherCapitalAllowance": 400.02,
     |    "propertyIncomeAllowance": 10.02
     |}
     |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[AnnualAllowances] shouldBe annualAllowances
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(annualAllowances) shouldBe writesJson
      }
    }
  }

}
