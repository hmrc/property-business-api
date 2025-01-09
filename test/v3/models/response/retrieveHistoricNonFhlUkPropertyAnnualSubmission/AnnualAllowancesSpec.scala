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
import shared.utils.UnitSpec
import v3.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.AnnualAllowances

class AnnualAllowancesSpec extends UnitSpec {

  private def decimal(value: String): Option[BigDecimal] = Option(BigDecimal(value))

  val downstreamJson: JsValue = Json.parse("""
                                             |   {
                                             |      "annualInvestmentAllowance": 200.00,
                                             |      "otherCapitalAllowance": 300.00,
                                             |      "zeroEmissionGoodsVehicleAllowance": 400.00,
                                             |      "businessPremisesRenovationAllowance": 200.00,
                                             |      "costOfReplacingDomGoods": 200.00,
                                             |      "propertyIncomeAllowance": 30.02
                                             |   }
                                             |""".stripMargin)

  val mtdJson: JsValue = Json.parse("""
                                      |   {
                                      |     "annualInvestmentAllowance": 200.00,
                                      |      "otherCapitalAllowance": 300.00,
                                      |      "zeroEmissionGoodsVehicleAllowance": 400.00,
                                      |      "businessPremisesRenovationAllowance": 200.00,
                                      |      "costOfReplacingDomesticGoods": 200.00,
                                      |      "propertyIncomeAllowance": 30.02
                                      |   }
                                      |""".stripMargin)

  val model: AnnualAllowances = AnnualAllowances(
    annualInvestmentAllowance = decimal("200.00"),
    otherCapitalAllowance = decimal("300.00"),
    zeroEmissionGoodsVehicleAllowance = decimal("400.00"),
    businessPremisesRenovationAllowance = decimal("200.00"),
    costOfReplacingDomesticGoods = decimal("200.00"),
    propertyIncomeAllowance = decimal("30.02")
  )

  "reads" should {
    "return an expected object" when {
      "a correct Json is passed as an input" in {
        val result = downstreamJson.as[AnnualAllowances]
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
