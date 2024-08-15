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

import api.models.utils.JsonErrorValidators
import play.api.libs.json.Json
import support.UnitSpec

class Def1_Retrieve_ForeignFhlEeaEntrySpec extends UnitSpec with JsonErrorValidators {

  private val foreignFhlEea = Def1_Retrieve_ForeignFhlEeaEntry(
    Some(
      Def1_Retrieve_ForeignFhlEeaAdjustments(
        Some(100.25),
        Some(100.25),
        Some(true)
      )),
    Some(
      Def1_Retrieve_ForeignFhlEeaAllowances(
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25),
        Some(100.25)
      ))
  )

  private val foreignFhlEeaNoAdjustments = Def1_Retrieve_ForeignFhlEeaEntry(
    None,
    Some(Def1_Retrieve_ForeignFhlEeaAllowances(Some(100.25), Some(100.25), Some(100.25), Some(100.25), Some(100.25)))
  )

  private val foreignFhlEeaNoAllowances = Def1_Retrieve_ForeignFhlEeaEntry(
    Some(
      Def1_Retrieve_ForeignFhlEeaAdjustments(
        Some(100.25),
        Some(100.25),
        Some(true)
      )),
    None
  )

  private val ifsJsonBody = Json.parse(
    """
    |{
    |   "adjustments":{
    |      "privateUseAdjustment":100.25,
    |      "balancingCharge":100.25,
    |      "periodOfGraceAdjustment":true
    |   },
    |   "allowances":{
    |      "annualInvestmentAllowance":100.25,
    |      "otherCapitalAllowance":100.25,
    |      "electricChargePointAllowance":100.25,
    |      "zeroEmissionsCarAllowance":100.25,
    |      "propertyAllowance": 100.25
    |   }
    |}
  """.stripMargin
  )

  private val jsonBody = Json.parse(
    """
      |{
      |   "adjustments":{
      |      "privateUseAdjustment":100.25,
      |      "balancingCharge":100.25,
      |      "periodOfGraceAdjustment":true
      |   },
      |   "allowances":{
      |      "annualInvestmentAllowance":100.25,
      |      "otherCapitalAllowance":100.25,
      |      "electricChargePointAllowance":100.25,
      |      "zeroEmissionsCarAllowance":100.25,
      |      "propertyIncomeAllowance": 100.25
      |   }
      |}
  """.stripMargin
  )

  private val ifsJsonBodyNoAdjustments = Json.parse(
    """
    |{
    |   "allowances":{
    |      "annualInvestmentAllowance":100.25,
    |      "otherCapitalAllowance":100.25,
    |      "electricChargePointAllowance":100.25,
    |      "zeroEmissionsCarAllowance":100.25,
    |      "propertyAllowance": 100.25
    |   }
    |}
  """.stripMargin
  )

  private val jsonBodyNoAdjustments = Json.parse(
    """
      |{
      |   "allowances":{
      |      "annualInvestmentAllowance":100.25,
      |      "otherCapitalAllowance":100.25,
      |      "electricChargePointAllowance":100.25,
      |      "zeroEmissionsCarAllowance":100.25,
      |      "propertyIncomeAllowance": 100.25
      |   }
      |}
  """.stripMargin
  )

  private val jsonBodyNoAllowances = Json.parse(
    """
    |{
    |   "adjustments":{
    |       "privateUseAdjustment":100.25,
    |       "balancingCharge":100.25,
    |       "periodOfGraceAdjustment":true
    |   }
    |}
  """.stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        ifsJsonBody.as[Def1_Retrieve_ForeignFhlEeaEntry] shouldBe foreignFhlEea
      }
      "return a valid model with no adjustments object" in {
        ifsJsonBodyNoAdjustments.as[Def1_Retrieve_ForeignFhlEeaEntry] shouldBe foreignFhlEeaNoAdjustments
      }
      "return a valid model with no allowances object" in {
        jsonBodyNoAllowances.as[Def1_Retrieve_ForeignFhlEeaEntry] shouldBe foreignFhlEeaNoAllowances
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
