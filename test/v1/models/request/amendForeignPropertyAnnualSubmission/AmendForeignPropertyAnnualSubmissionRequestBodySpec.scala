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

package v1.models.request.amendForeignPropertyAnnualSubmission

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaAdjustments, ForeignFhlEeaAllowances}
import v1.models.request.amendForeignPropertyAnnualSubmission.foreignProperty.{ForeignPropertyAdjustments, ForeignPropertyAllowances, ForeignPropertyEntry}
import v1.models.utils.JsonErrorValidators

class AmendForeignPropertyAnnualSubmissionRequestBodySpec extends UnitSpec with JsonErrorValidators {

  val amendForeignPropertyAnnualSubmissionRequestBody =
    AmendForeignPropertyAnnualSubmissionRequestBody(
      Some(ForeignFhlEea(
        Some(ForeignFhlEeaAdjustments(
          Some(100.25),
          Some(100.25),
          Some(true)
        )),
        Some(ForeignFhlEeaAllowances(
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25)
        ))
      )),
      Some(Seq(ForeignPropertyEntry(
        "GER",
        Some(ForeignPropertyAdjustments(
          Some(100.25),
          Some(100.25))),
        Some(ForeignPropertyAllowances(
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25)
        )))))
    )

  val amendForeignPropertyAnnualSubmissionRequestBodyMinimum =
    AmendForeignPropertyAnnualSubmissionRequestBody(
      None,
      Some(Seq(ForeignPropertyEntry(
        "GER",
        None,
        None)))
    )

  val jsonBody = Json.parse(
    """
      |{
      |   "foreignFhlEea":
      |      {
      |         "adjustments":{
      |            "privateUseAdjustment":100.25,
      |            "balancingCharge":100.25,
      |            "periodOfGraceAdjustment":true
      |         },
      |         "allowances":{
      |            "annualInvestmentAllowance":100.25,
      |            "otherCapitalAllowance":100.25,
      |            "propertyAllowance":100.25,
      |            "electricChargePointAllowance":100.25
      |         }
      |      },
      |   "foreignProperty":[
      |      {
      |         "countryCode":"GER",
      |         "adjustments":{
      |            "privateUseAdjustment":100.25,
      |            "balancingCharge":100.25
      |         },
      |         "allowances":{
      |            "annualInvestmentAllowance":100.25,
      |            "costOfReplacingDomesticItems":100.25,
      |            "zeroEmissionsGoodsVehicleAllowance":100.25,
      |            "propertyAllowance":100.25,
      |            "otherCapitalAllowance":100.25,
      |            "electricChargePointAllowance":100.25
      |         }
      |      }
      |   ]
      |}
      |""".stripMargin)

  val jsonBodyMinimum = Json.parse(
    """
      |{
      |   "foreignProperty":[
      |      {
      |         "countryCode":"GER"
      |      }
      |   ]
      |}
      |""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        jsonBody.as[AmendForeignPropertyAnnualSubmissionRequestBody] shouldBe amendForeignPropertyAnnualSubmissionRequestBody
      }
      "return a valid model with minimum fields" in {
        jsonBodyMinimum.as[AmendForeignPropertyAnnualSubmissionRequestBody] shouldBe amendForeignPropertyAnnualSubmissionRequestBodyMinimum
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(amendForeignPropertyAnnualSubmissionRequestBody) shouldBe jsonBody
      }
      "return a valid minimum JSON" in {
        Json.toJson(amendForeignPropertyAnnualSubmissionRequestBodyMinimum) shouldBe jsonBodyMinimum
      }
    }
  }
}
