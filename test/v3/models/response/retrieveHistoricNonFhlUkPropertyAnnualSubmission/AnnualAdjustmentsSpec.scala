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

package v3.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmission

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v3.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.{AnnualAdjustments, RentARoom}

class AnnualAdjustmentsSpec extends UnitSpec {

  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  val downstreamJson: JsValue = Json.parse("""
                                             |   {
                                             |      "lossBroughtForward": 200.00,
                                             |      "balancingCharge": 300.00,
                                             |      "privateUseAdjustment": 400.00,
                                             |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
                                             |      "nonResidentLandlord": true,
                                             |      "ukRentARoom": {
                                             |         "jointlyLet": true
                                             |      }
                                             |   }
                                             |""".stripMargin)

  val mtdJson: JsValue = Json.parse("""
                                             |   {
                                             |      "lossBroughtForward": 200.00,
                                             |      "balancingCharge": 300.00,
                                             |      "privateUseAdjustment": 400.00,
                                             |      "businessPremisesRenovationAllowanceBalancingCharges": 80.02,
                                             |      "nonResidentLandlord": true,
                                             |      "rentARoom": {
                                             |         "jointlyLet": true
                                             |      }
                                             |   }
                                             |""".stripMargin)

  val model: AnnualAdjustments = AnnualAdjustments(
    lossBroughtForward = decimal("200.00"),
    balancingCharge = decimal("300.00"),
    privateUseAdjustment = decimal("400.00"),
    businessPremisesRenovationAllowanceBalancingCharges = decimal("80.02"),
    nonResidentLandlord = true,
    rentARoom = Option(RentARoom(jointlyLet = true))
  )

  "reads" should {
    "return an expected object" when {
      "a correct Json is passed as an input" in {
        val result = downstreamJson.as[AnnualAdjustments]
        result shouldBe model
      }
    }
  }

  "writes" should {
    "return a correct Json" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }

}
