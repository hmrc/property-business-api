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

package v6.createAmendForeignPropertyAnnualSubmission.def1.model.request

import play.api.libs.json.{JsValue, Json}
import v6.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignFhlEea.{
  Def1_Create_Amend_ForeignFhlEea,
  Def1_Create_Amend_ForeignFhlEeaAdjustments,
  Def1_Create_Amend_ForeignFhlEeaAllowances
}
import v6.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignProperty.{
  Def1_Create_Amend_ForeignAdjustments,
  Def1_Create_Amend_ForeignAllowances,
  Def1_Create_Amend_ForeignEntry
}

trait Def1_Fixtures extends StructuredBuildingAllowanceFixture {

  val foreignFhlEeaAdjustments: Def1_Create_Amend_ForeignFhlEeaAdjustments =
    Def1_Create_Amend_ForeignFhlEeaAdjustments(
      privateUseAdjustment = Some(1.25),
      balancingCharge = Some(2.25),
      periodOfGraceAdjustment = true
    )

  val foreignFhlEeaAdjustmentsMtdJson: JsValue =
    Json.parse("""
                  |{
                  |    "privateUseAdjustment":1.25,
                  |    "balancingCharge":2.25,
                  |    "periodOfGraceAdjustment":true
                  |}
                  |""".stripMargin)

  val foreignFhlEeaAdjustmentsDownstreamJson: JsValue = foreignFhlEeaAdjustmentsMtdJson

  val foreignFhlEeaAllowances: Def1_Create_Amend_ForeignFhlEeaAllowances =
    Def1_Create_Amend_ForeignFhlEeaAllowances(
      annualInvestmentAllowance = Some(1.25),
      otherCapitalAllowance = Some(2.25),
      electricChargePointAllowance = Some(3.25),
      zeroEmissionsCarAllowance = Some(4.25),
      propertyIncomeAllowance = Some(5.25)
    )

  val foreignFhlEeaAllowancesMtdJson: JsValue = Json.parse("""
      |{
      |    "annualInvestmentAllowance":1.25,
      |    "otherCapitalAllowance":2.25,
      |    "electricChargePointAllowance":3.25,
      |    "zeroEmissionsCarAllowance":4.25,
      |    "propertyIncomeAllowance":5.25
      |}
      |""".stripMargin)

  val foreignFhlEeaAllowancesDownstreamJson: JsValue = Json.parse("""
     |{
     |    "annualInvestmentAllowance":1.25,
     |    "otherCapitalAllowance":2.25,
     |    "electricChargePointAllowance":3.25,
     |    "zeroEmissionsCarAllowance":4.25,
     |    "propertyAllowance":5.25
     |}
     |""".stripMargin)

  val foreignFhlEea: Def1_Create_Amend_ForeignFhlEea =
    Def1_Create_Amend_ForeignFhlEea(
      adjustments = Some(foreignFhlEeaAdjustments),
      allowances = Some(foreignFhlEeaAllowances)
    )

  val foreignFhlEeaMtdJson: JsValue = Json.parse(s"""
      |{
      |   "adjustments": $foreignFhlEeaAdjustmentsMtdJson,
      |   "allowances": $foreignFhlEeaAllowancesMtdJson
      |}
      |""".stripMargin)

  val foreignFhlEeaDownstreamJson: JsValue = Json.parse(s"""
       |{
       |   "adjustments": $foreignFhlEeaAdjustmentsDownstreamJson,
       |   "allowances": $foreignFhlEeaAllowancesDownstreamJson
       |}
       |""".stripMargin)

  val foreignAdjustments: Def1_Create_Amend_ForeignAdjustments =
    Def1_Create_Amend_ForeignAdjustments(
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

  val foreignAllowances: Def1_Create_Amend_ForeignAllowances =
    Def1_Create_Amend_ForeignAllowances(
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

  val foreignEntry: Def1_Create_Amend_ForeignEntry =
    Def1_Create_Amend_ForeignEntry(
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

  val createAmendForeignPropertyAnnualSubmissionRequestBody: Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
      foreignFhlEea = Some(foreignFhlEea),
      foreignProperty = Some(List(foreignEntry))
    )

  val createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson: JsValue = Json.parse(s"""
      |{
      |   "foreignFhlEea": $foreignFhlEeaMtdJson,
      |   "foreignProperty":[ $foreignEntryMtdJson ]
      |}
      |""".stripMargin)

  val createAmendForeignPropertyAnnualSubmissionRequestBodyDownstreamJson: JsValue = Json.parse(s"""
      |{
      |   "foreignFhlEea": $foreignFhlEeaDownstreamJson,
      |   "foreignProperty":[ $foreignEntryDownstreamJson ]
      |}
      |""".stripMargin)

}
