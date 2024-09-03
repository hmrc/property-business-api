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

package v5.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignFhlEea

import play.api.libs.json.Json
import support.UnitSpec

class Def1_Retrieve_ForeignFhlEeaEntryAllowancesSpec extends UnitSpec {

  private val foreignFhlEeaAllowances = Def1_Retrieve_ForeignFhlEeaAllowances(
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(100.25),
    Some(100.25)
  )

  private val jsonBody = Json.parse(
    """
      |{
      |    "annualInvestmentAllowance":100.25,
      |    "otherCapitalAllowance":100.25,
      |    "electricChargePointAllowance":100.25,
      |    "zeroEmissionsCarAllowance":100.25,
      |    "propertyIncomeAllowance": 100.25
      |}
    """.stripMargin
  )

  private val ifsJsonBody = Json.parse(
    """
      |{
      |    "annualInvestmentAllowance":100.25,
      |    "otherCapitalAllowance":100.25,
      |    "electricChargePointAllowance":100.25,
      |    "zeroEmissionsCarAllowance":100.25,
      |    "propertyAllowance": 100.25
      |}
    """.stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        ifsJsonBody.as[Def1_Retrieve_ForeignFhlEeaAllowances] shouldBe foreignFhlEeaAllowances
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignFhlEeaAllowances) shouldBe jsonBody
      }
    }
  }

}
