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

package v1.models.request.amendForeignPropertyAnnualSubmission.foreignProperty

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class ForeignPropertyEntrySpec extends UnitSpec with JsonErrorValidators {

  val foreignProperty: ForeignPropertyEntry =
    ForeignPropertyEntry(
      "GER",
      Some(
        ForeignPropertyAdjustments(
          Some(100.25),
          Some(100.25)
        )),
      Some(
        ForeignPropertyAllowances(
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(200.65),
          Some(100.25)
        ))
    )

  val foreignPropertyNoAdjustments: ForeignPropertyEntry =
    ForeignPropertyEntry(
      "GER",
      None,
      Some(
        ForeignPropertyAllowances(
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(300.45),
          Some(100.25)
        ))
    )

  val foreignPropertyNoAllowances: ForeignPropertyEntry =
    ForeignPropertyEntry(
      "GER",
      Some(
        ForeignPropertyAdjustments(
          Some(100.25),
          Some(100.25)
        )),
      None
    )

  val jsonBody: JsValue = Json.parse(
    """
      |{
      |   "countryCode":"GER",
      |   "adjustments":{
      |      "privateUseAdjustment":100.25,
      |      "balancingCharge":100.25
      |   },
      |   "allowances":{
      |      "annualInvestmentAllowance":100.25,
      |      "costOfReplacingDomesticItems":100.25,
      |      "zeroEmissionsGoodsVehicleAllowance":100.25,
      |      "propertyAllowance":100.25,
      |      "otherCapitalAllowance":100.25,
      |      "structureAndBuildingAllowance":200.65,
      |      "electricChargePointAllowance":100.25
      |   }
      |}
    """.stripMargin
  )

  val jsonBodyNoAdjustments: JsValue = Json.parse(
    """
      |{
      |   "countryCode":"GER",
      |   "allowances":{
      |      "annualInvestmentAllowance":100.25,
      |      "costOfReplacingDomesticItems":100.25,
      |      "zeroEmissionsGoodsVehicleAllowance":100.25,
      |      "propertyAllowance":100.25,
      |      "otherCapitalAllowance":100.25,
      |      "structureAndBuildingAllowance":300.45,
      |      "electricChargePointAllowance":100.25
      |   }
      |}
    """.stripMargin
  )

  val jsonBodyNoAllowances: JsValue = Json.parse(
    """
      |{
      |   "countryCode":"GER",
      |   "adjustments":{
      |      "privateUseAdjustment":100.25,
      |      "balancingCharge":100.25
      |   }
      |}
    """.stripMargin
  )

  val emptyJson: JsValue = Json.parse(
    """
      |{}
    """.stripMargin
  )

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        jsonBody.as[ForeignPropertyEntry] shouldBe foreignProperty
      }
      "return a valid model with no adjustments object" in {
        jsonBodyNoAdjustments.as[ForeignPropertyEntry] shouldBe foreignPropertyNoAdjustments
      }
      "return a valid model with no allowances object" in {
        jsonBodyNoAllowances.as[ForeignPropertyEntry] shouldBe foreignPropertyNoAllowances
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignProperty) shouldBe jsonBody
      }
      "return a valid JSON with no adjustments" in {
        Json.toJson(foreignPropertyNoAdjustments) shouldBe jsonBodyNoAdjustments
      }
      "return a valid JSON with no allowances" in {
        Json.toJson(foreignPropertyNoAllowances) shouldBe jsonBodyNoAllowances
      }
    }
  }
}
