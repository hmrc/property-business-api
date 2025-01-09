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

package v4.models.request.createAmendForeignPropertyAnnualSubmission

import play.api.libs.json.{JsValue, Json}
import v4.createAmendForeignPropertyAnnualSubmission.def1.model.request.StructuredBuildingAllowanceFixture
import v4.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignFhlEea._
import v4.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignNonFhl._
import v4.createAmendForeignPropertyAnnualSubmission.model.request._

trait CreateAmendForeignPropertyAnnualSubmissionFixture extends StructuredBuildingAllowanceFixture {

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

  val foreignNonFhlAdjustments: Def1_Create_Amend_ForeignNonFhlAdjustments =
    Def1_Create_Amend_ForeignNonFhlAdjustments(
      privateUseAdjustment = Some(1.25),
      balancingCharge = Some(2.25)
    )

  val foreignNonFhlAdjustmentsMtdJson: JsValue = Json.parse("""
                                                              |{
                                                              |    "privateUseAdjustment":1.25,
                                                              |    "balancingCharge":2.25
                                                              |}
                                                              |""".stripMargin)

  val foreignNonFhlAdjustmentsDownstreamJson: JsValue = foreignNonFhlAdjustmentsMtdJson

  val foreignNonFhlAllowances: Def1_Create_Amend_ForeignNonFhlAllowances =
    Def1_Create_Amend_ForeignNonFhlAllowances(
      annualInvestmentAllowance = Some(1.25),
      costOfReplacingDomesticItems = Some(2.25),
      zeroEmissionsGoodsVehicleAllowance = Some(3.25),
      otherCapitalAllowance = Some(4.25),
      electricChargePointAllowance = Some(5.25),
      zeroEmissionsCarAllowance = Some(6.25),
      propertyIncomeAllowance = Some(7.25),
      structuredBuildingAllowance = Some(List(structuredBuildingAllowance))
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

  val foreignNonFhlEntry: Def1_Create_Amend_ForeignNonFhlEntry =
    Def1_Create_Amend_ForeignNonFhlEntry(
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

  val createAmendForeignPropertyAnnualSubmissionRequestBody: Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    Def1_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
      foreignFhlEea = Some(foreignFhlEea),
      foreignNonFhlProperty = Some(List(foreignNonFhlEntry))
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
