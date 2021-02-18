/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.request.amendForeignPropertyAnnualSubmission.foreignProperty

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class ForeignPropertyAllowancesSpec extends UnitSpec with JsonErrorValidators{

  val foreignPropertyAllowances =
    ForeignPropertyAllowances(
      Some(100.25),
      Some(100.25),
      Some(100.25),
      Some(100.25),
      Some(100.25),
      Some(100.25)
    )

  val jsonBody = Json.parse(
    """
      |{
      |    "annualInvestmentAllowance":100.25,
      |    "costOfReplacingDomesticItems":100.25,
      |    "zeroEmissionsGoodsVehicleAllowance":100.25,
      |    "propertyAllowance":100.25,
      |    "otherCapitalAllowance":100.25,
      |    "electricChargePointAllowance":100.25
      |}
      |""".stripMargin)

  val emptyJson = Json.parse(
    """
      |{}
      |""".stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        jsonBody.as[ForeignPropertyAllowances] shouldBe foreignPropertyAllowances
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignPropertyAllowances) shouldBe jsonBody
      }
    }
  }
  "isEmpty" when {
    "passed a valid model" should {
      "return false" in {
        jsonBody.as[ForeignPropertyAllowances].isEmpty shouldBe false
      }
    }
    "passed an empty model" should {
      "return true" in {
        emptyJson.as[ForeignPropertyAllowances].isEmpty shouldBe true
      }
    }
  }
}
