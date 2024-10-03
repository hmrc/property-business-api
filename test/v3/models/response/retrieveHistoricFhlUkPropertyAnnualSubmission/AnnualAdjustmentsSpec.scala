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

package v3.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission

import play.api.libs.json.Json
import support.UnitSpec

class AnnualAdjustmentsSpec extends UnitSpec {

  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  private val annualAdjustments =
    AnnualAdjustments(decimal("200.00"), decimal("300.00"), decimal("400.00"), true, decimal("500.02"), true, Option(RentARoom(jointlyLet = false)))

  private val writesJson = Json.parse(
    """
      |{
      |    "lossBroughtForward": 200.00,
      |    "privateUseAdjustment": 300.00,
      |    "balancingCharge": 400.00,
      |    "periodOfGraceAdjustment": true,
      |    "businessPremisesRenovationAllowanceBalancingCharges": 500.02,
      |    "nonResidentLandlord": true,
      |    "rentARoom": {
      |       "jointlyLet": false
      |    }
      |}
      |""".stripMargin
  )

  private val readsJson = Json.parse("""
     |{
     |    "lossBroughtForward": 200.00,
     |    "privateUseAdjustment": 300.00,
     |    "balancingCharge": 400.00,
     |    "periodOfGraceAdjustment": true,
     |    "businessPremisesRenovationAllowanceBalancingCharges": 500.02,
     |    "nonResidentLandlord": true,
     |    "ukRentARoom": {
     |       "jointlyLet": false
     |    }
     |}
     |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        readsJson.as[AnnualAdjustments] shouldBe annualAdjustments
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(annualAdjustments) shouldBe writesJson
      }
    }
  }

}
