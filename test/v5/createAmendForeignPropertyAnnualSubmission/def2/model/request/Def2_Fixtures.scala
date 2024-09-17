/*
 * Copyright 2024 HM Revenue & Customs
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

package v5.createAmendForeignPropertyAnnualSubmission.def2.model.request

import play.api.libs.json.{JsValue, Json}
import v5.createAmendForeignPropertyAnnualSubmission.def2.model.request.def2_foreignProperty.{
  Def2_Create_Amend_ForeignAdjustments,
  Def2_Create_Amend_ForeignAllowances,
  Def2_Create_Amend_ForeignEntry
}

trait Def2_Fixtures extends StructuredBuildingAllowanceFixture {

  val foreignAdjustments: Def2_Create_Amend_ForeignAdjustments =
    Def2_Create_Amend_ForeignAdjustments(
      privateUseAdjustment = Some(1.25),
      balancingCharge = Some(2.25)
    )

  val foreignAdjustmentsMtdJson: JsValue = Json.parse("""
      |{
      |    "privateUseAdjustment":1.25,
      |    "balancingCharge":2.25
      |}
      |""".stripMargin)

  val foreignAdjustmentsDownstreamJson: JsValue = foreignAdjustmentsMtdJson

  val foreignAllowances: Def2_Create_Amend_ForeignAllowances =
    Def2_Create_Amend_ForeignAllowances(
      annualInvestmentAllowance = Some(1.25),
      costOfReplacingDomesticItems = Some(2.25),
      zeroEmissionsGoodsVehicleAllowance = Some(3.25),
      otherCapitalAllowance = Some(4.25),
      electricChargePointAllowance = Some(5.25),
      zeroEmissionsCarAllowance = Some(6.25),
      propertyIncomeAllowance = Some(7.25),
      structuredBuildingAllowance = Some(List(structuredBuildingAllowance))
    )

  val foreignAllowancesMtdJson: JsValue = Json.parse(s"""
      |{
      |    "annualInvestmentAllowance":1.25,
      |    "costOfReplacingDomesticItems":2.25,
      |    "zeroEmissionsGoodsVehicleAllowance":3.25,
      |    "otherCapitalAllowance":4.25,
      |    "electricChargePointAllowance":5.25,
      |    "zeroEmissionsCarAllowance":6.25,
      |    "propertyIncomeAllowance":7.25,
      |    "structuredBuildingAllowance": [$structuredBuildingAllowanceMtdJson]
      |}
      |""".stripMargin)

  val foreignAllowancesDownstreamJson: JsValue = Json.parse(s"""
      |{
      |    "annualInvestmentAllowance":1.25,
      |    "costOfReplacingDomesticItems":2.25,
      |    "zeroEmissionsGoodsVehicleAllowance":3.25,
      |    "otherCapitalAllowance":4.25,
      |    "electricChargePointAllowance":5.25,
      |    "zeroEmissionsCarAllowance":6.25,
      |    "propertyAllowance":7.25,
      |    "structuredBuildingAllowance": [$structuredBuildingAllowanceDownstreamJson]
      |}
      |""".stripMargin)

  val foreignEntry: Def2_Create_Amend_ForeignEntry =
    Def2_Create_Amend_ForeignEntry(
      countryCode = "GER",
      adjustments = Some(foreignAdjustments),
      allowances = Some(foreignAllowances)
    )

  val foreignEntryMtdJson: JsValue = Json.parse(s"""
      |{
      |   "countryCode":"GER",
      |   "adjustments": $foreignAdjustmentsMtdJson,
      |   "allowances": $foreignAllowancesMtdJson
      |}
      |""".stripMargin)

  val foreignEntryDownstreamJson: JsValue = Json.parse(s"""
     |{
     |   "countryCode":"GER",
     |   "adjustments": $foreignAdjustmentsDownstreamJson,
     |   "allowances": $foreignAllowancesDownstreamJson
     |}
     |""".stripMargin)

  val def2_createAmendForeignPropertyAnnualSubmissionRequestBody: Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
      foreignProperty = Some(List(foreignEntry))
    )

  val def2_createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson: JsValue = Json.parse(s"""
      |{
      |   "foreignProperty":[ $foreignEntryMtdJson ]
      |}
      |""".stripMargin)

  val def2_createAmendForeignPropertyAnnualSubmissionRequestBodyDownstreamJson: JsValue = Json.parse(s"""
      |{
      |   "foreignProperty":[ $foreignEntryDownstreamJson ]
      |}
      |""".stripMargin)

}
