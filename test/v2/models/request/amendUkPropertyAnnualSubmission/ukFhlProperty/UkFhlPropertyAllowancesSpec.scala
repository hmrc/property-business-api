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

package v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec

class UkFhlPropertyAllowancesSpec extends UnitSpec {

  val requestBody: UkFhlPropertyAllowances =
    UkFhlPropertyAllowances(
      Some(1000.50),
      Some(1000.60),
      Some(1000.70),
      Some(1000.80),
      Some(1000.90),
      None
    )

  val validMtdJson: JsValue = Json.parse("""
      |{
      |  "annualInvestmentAllowance": 1000.50,
      |  "businessPremisesRenovationAllowance": 1000.60,
      |  "otherCapitalAllowance": 1000.70,
      |  "electricChargePointAllowance": 1000.80,
      |  "zeroEmissionsCarAllowance": 1000.90
      |}
      |""".stripMargin)

  val validDownstreamJson: JsValue = Json.parse("""
      |{
      |  "annualInvestmentAllowance": 1000.50,
      |  "businessPremisesRenovationAllowance": 1000.60,
      |  "otherCapitalAllowance": 1000.70,
      |  "electricChargePointAllowance": 1000.80,
      |  "zeroEmissionsCarAllowance": 1000.90
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[UkFhlPropertyAllowances] shouldBe requestBody
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBody) shouldBe validDownstreamJson
      }
    }
  }
}
