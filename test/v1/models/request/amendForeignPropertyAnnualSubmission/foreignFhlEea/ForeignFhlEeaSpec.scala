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

package v1.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class ForeignFhlEeaSpec extends UnitSpec with JsonErrorValidators {

  val foreignFhlEea =
    ForeignFhlEea(
      Some(
        ForeignFhlEeaAdjustments(
          Some(100.25),
          Some(100.25),
          Some(true)
        )),
      Some(
        ForeignFhlEeaAllowances(
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25)
        ))
    )

  val foreignFhlEeaNoAdjustments =
    ForeignFhlEea(
      None,
      Some(
        ForeignFhlEeaAllowances(
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25)
        ))
    )

  val foreignFhlEeaNoAllowances =
    ForeignFhlEea(
      Some(
        ForeignFhlEeaAdjustments(
          Some(100.25),
          Some(100.25),
          Some(true)
        )),
      None
    )

  val jsonBody = Json.parse("""
      |{
      |   "adjustments":{
      |      "privateUseAdjustment":100.25,
      |      "balancingCharge":100.25,
      |      "periodOfGraceAdjustment":true
      |   },
      |   "allowances":{
      |      "annualInvestmentAllowance":100.25,
      |      "otherCapitalAllowance":100.25,
      |      "propertyAllowance":100.25,
      |      "electricChargePointAllowance":100.25
      |   }
      |}
      |""".stripMargin)

  val jsonBodyNoAdjustments = Json.parse("""
      |{
      |   "allowances":{
      |      "annualInvestmentAllowance":100.25,
      |      "otherCapitalAllowance":100.25,
      |      "propertyAllowance":100.25,
      |      "electricChargePointAllowance":100.25
      |   }
      |}
      |""".stripMargin)

  val jsonBodyNoAllowances = Json.parse("""
      |{
      |   "adjustments":{
      |       "privateUseAdjustment":100.25,
      |       "balancingCharge":100.25,
      |       "periodOfGraceAdjustment":true
      |   }
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        jsonBody.as[ForeignFhlEea] shouldBe foreignFhlEea
      }
      "return a valid model with no adjustments object" in {
        jsonBodyNoAdjustments.as[ForeignFhlEea] shouldBe foreignFhlEeaNoAdjustments
      }
      "return a valid model with no allowances object" in {
        jsonBodyNoAllowances.as[ForeignFhlEea] shouldBe foreignFhlEeaNoAllowances
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignFhlEea) shouldBe jsonBody
      }
      "return a valid JSON with no adjustments" in {
        Json.toJson(foreignFhlEeaNoAdjustments) shouldBe jsonBodyNoAdjustments
      }
      "return a valid JSON with no allowances" in {
        Json.toJson(foreignFhlEeaNoAllowances) shouldBe jsonBodyNoAllowances
      }
    }
  }

}
