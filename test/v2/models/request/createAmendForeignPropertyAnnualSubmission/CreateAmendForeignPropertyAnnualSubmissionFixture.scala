/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.models.request.createAmendForeignPropertyAnnualSubmission

import play.api.libs.json.{ JsValue, Json }
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignFhlEea.{ ForeignFhlEea, ForeignFhlEeaAdjustments, ForeignFhlEeaAllowances }
import v2.models.request.createAmendForeignPropertyAnnualSubmission.foreignNonFhl.{ ForeignNonFhlAdjustments, ForeignNonFhlAllowances, ForeignNonFhlEntry }
import v2.models.request.common.StructuredBuildingAllowanceFixture

trait CreateAmendForeignPropertyAnnualSubmissionFixture extends StructuredBuildingAllowanceFixture {

  val foreignFhlEeaAdjustments: ForeignFhlEeaAdjustments =
    ForeignFhlEeaAdjustments(
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

  val foreignFhlEeaAllowances: ForeignFhlEeaAllowances =
    ForeignFhlEeaAllowances(
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

  val foreignFhlEea: ForeignFhlEea =
    ForeignFhlEea(
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

  val foreignNonFhlAdjustments: ForeignNonFhlAdjustments =
    ForeignNonFhlAdjustments(
      privateUseAdjustment = Some(1.25),
      balancingCharge = Some(2.25)
    )

  val foreignNonFhlAdjustmentsMtdJson: JsValue        = Json.parse("""
      |{
      |    "privateUseAdjustment":1.25,
      |    "balancingCharge":2.25
      |}
      |""".stripMargin)
  val foreignNonFhlAdjustmentsDownstreamJson: JsValue = foreignNonFhlAdjustmentsMtdJson

  val foreignNonFhlAllowances: ForeignNonFhlAllowances =
    ForeignNonFhlAllowances(
      annualInvestmentAllowance = Some(1.25),
      costOfReplacingDomesticItems = Some(2.25),
      zeroEmissionsGoodsVehicleAllowance = Some(3.25),
      otherCapitalAllowance = Some(4.25),
      electricChargePointAllowance = Some(5.25),
      zeroEmissionsCarAllowance = Some(6.25),
      propertyIncomeAllowance = Some(7.25),
      structuredBuildingAllowance = Some(Seq(structuredBuildingAllowance))
    )

  val foreignNonFhlAllowancesMtdJson: JsValue = Json.parse(s"""
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

  val foreignNonFhlAllowancesDownstreamJson: JsValue = Json.parse(s"""
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

  val foreignNonFhlEntry: ForeignNonFhlEntry =
    ForeignNonFhlEntry(
      countryCode = "GER",
      adjustments = Some(foreignNonFhlAdjustments),
      allowances = Some(foreignNonFhlAllowances)
    )

  val foreignNonFhlEntryMtdJson: JsValue = Json.parse(s"""
      |{
      |   "countryCode":"GER",
      |   "adjustments": $foreignNonFhlAdjustmentsMtdJson,
      |   "allowances": $foreignNonFhlAllowancesMtdJson
      |}
      |""".stripMargin)

  val foreignNonFhlEntryDownstreamJson: JsValue = Json.parse(s"""
     |{
     |   "countryCode":"GER",
     |   "adjustments": $foreignNonFhlAdjustmentsDownstreamJson,
     |   "allowances": $foreignNonFhlAllowancesDownstreamJson
     |}
     |""".stripMargin)

  val createAmendForeignPropertyAnnualSubmissionRequestBody: CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    CreateAmendForeignPropertyAnnualSubmissionRequestBody(
      foreignFhlEea = Some(foreignFhlEea),
      foreignNonFhlProperty = Some(Seq(foreignNonFhlEntry))
    )

  val createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson: JsValue = Json.parse(s"""
      |{
      |   "foreignFhlEea": $foreignFhlEeaMtdJson,
      |   "foreignNonFhlProperty":[ $foreignNonFhlEntryMtdJson ]
      |}
      |""".stripMargin)

  val createAmendForeignPropertyAnnualSubmissionRequestBodyDownstreamJson: JsValue = Json.parse(s"""
      |{
      |   "foreignFhlEea": $foreignFhlEeaDownstreamJson,
      |   "foreignProperty":[ $foreignNonFhlEntryDownstreamJson ]
      |}
      |""".stripMargin)
}
