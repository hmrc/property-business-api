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

package v3.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class HistoricNonFhlAnnualAllowancesSpec extends UnitSpec {

  val annualAllowances: HistoricNonFhlAnnualAllowances =
    HistoricNonFhlAnnualAllowances(
      annualInvestmentAllowance = Some(100.00),
      zeroEmissionGoodsVehicleAllowance = Some(200.00),
      businessPremisesRenovationAllowance = Some(300.00),
      otherCapitalAllowance = Some(400.00),
      costOfReplacingDomesticGoods = Some(500.00),
      propertyIncomeAllowance = Some(600.00)
    )

  val validMtdJson: JsValue = Json.parse(
    """
      |{
      |      "annualInvestmentAllowance": 100.00,
      |      "zeroEmissionGoodsVehicleAllowance": 200.00,
      |      "businessPremisesRenovationAllowance": 300.00,
      |      "otherCapitalAllowance": 400.00,
      |      "costOfReplacingDomesticGoods": 500.00,
      |      "propertyIncomeAllowance": 600.00
      |}
      |""".stripMargin
  )

  val validDownstreamJson: JsValue = Json.parse(
    """
      |{
      |      "annualInvestmentAllowance": 100.00,
      |      "zeroEmissionGoodsVehicleAllowance": 200.00,
      |      "businessPremisesRenovationAllowance": 300.00,
      |      "otherCapitalAllowance": 400.00,
      |      "costOfReplacingDomGoods": 500.00,
      |      "propertyIncomeAllowance": 600.00
      |}
      |""".stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        validMtdJson.as[HistoricNonFhlAnnualAllowances] shouldBe annualAllowances
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(annualAllowances) shouldBe validDownstreamJson
      }
    }
  }

}
