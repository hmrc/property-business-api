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

package v2.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission

import config.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class RetrieveHistoricFhlUkPropertyAnnualSubmissionResponseSpec extends UnitSpec with MockAppConfig {

  val downstreamJson: JsValue = Json.parse("""
      |{
      |   "annualAdjustments": {
      |      "lossBroughtForward": 200.00,
      |      "privateUseAdjustment": 300.00,
      |      "balancingCharge": 400.00,
      |      "periodOfGraceAdjustment": true,
      |      "businessPremisesRenovationAllowanceBalancingCharges": 500.02,
      |      "nonResidentLandlord": true,
      |      "ukRentARoom": {
      |         "jointlyLet": false
      |      }
      |   },
      |   "annualAllowances": {
      |      "annualInvestmentAllowance": 200.00,
      |      "businessPremisesRenovationAllowance": 300.00,
      |      "otherCapitalAllowance": 400.02,
      |      "propertyIncomeAllowance": 10.02
      |   }
      |}
      |""".stripMargin)

  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  val model = RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse(
    Some(
      AnnualAdjustments(
        decimal("200.00"),
        decimal("300.00"),
        decimal("400.00"),
        true,
        decimal("500.02"),
        true,
        Option(RentARoom(jointlyLet = false)))),
    Some(AnnualAllowances(decimal("200.00"), decimal("300.00"), decimal("400.02"), decimal("10.02")))
  )

  "reads" should {
    "read the JSON object from downstream into a case class" in {
      val result = downstreamJson.as[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse]
      result shouldBe model
    }
  }

}
