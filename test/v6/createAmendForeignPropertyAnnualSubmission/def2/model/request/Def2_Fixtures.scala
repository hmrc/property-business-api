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

package v6.createAmendForeignPropertyAnnualSubmission.def2.model.request

import play.api.libs.json.{JsValue, Json}
import v6.createAmendForeignPropertyAnnualSubmission.def2.model.request.def2_foreignProperty.{
  Def2_Create_Amend_ForeignAdjustments,
  Def2_Create_Amend_ForeignAllowances,
  Def2_Create_Amend_ForeignEntry
}

trait Def2_Fixtures extends Def2_StructuredBuildingAllowanceFixture {

  val def2_foreignAdjustments: Def2_Create_Amend_ForeignAdjustments =
    Def2_Create_Amend_ForeignAdjustments(
      privateUseAdjustment = Some(1.25),
      balancingCharge = Some(2.25)
    )

  val def2_foreignAdjustmentsMtdJson: JsValue = Json.parse("""
      |{
      |    "privateUseAdjustment":1.25,
      |    "balancingCharge":2.25
      |}
      |""".stripMargin)

  val def2_foreignAdjustmentsDownstreamJson: JsValue = def2_foreignAdjustmentsMtdJson

  val def2_foreignAllowances: Def2_Create_Amend_ForeignAllowances =
    Def2_Create_Amend_ForeignAllowances(
      annualInvestmentAllowance = Some(1.25),
      costOfReplacingDomesticItems = Some(2.25),
      otherCapitalAllowance = Some(4.25),
      zeroEmissionsCarAllowance = Some(6.25),
      propertyIncomeAllowance = None,
      structuredBuildingAllowance = Some(List(def2_structuredBuildingAllowance))
    )

  val def2_foreignAllowancesMtdJson: JsValue = Json.parse(s"""
      |{
      |    "annualInvestmentAllowance":1.25,
      |    "costOfReplacingDomesticItems":2.25,
      |    "otherCapitalAllowance":4.25,
      |    "zeroEmissionsCarAllowance":6.25,
      |    "structuredBuildingAllowance": [$def2_structuredBuildingAllowanceMtdJson]
      |}
      |""".stripMargin)

  val def2_foreignAllowancesDownstreamJson: JsValue = Json.parse(s"""
      |{
      |    "annualInvestmentAllowance":1.25,
      |    "costOfReplacingDomesticItems":2.25,
      |    "otherCapitalAllowance":4.25,
      |    "zeroEmissionsCarAllowance":6.25,
      |    "structuredBuildingAllowance": [$def2_structuredBuildingAllowanceDownstreamJson]
      |}
      |""".stripMargin)

  val def2_foreignEntry: Def2_Create_Amend_ForeignEntry =
    Def2_Create_Amend_ForeignEntry(
      countryCode = "IND",
      adjustments = Some(def2_foreignAdjustments),
      allowances = Some(def2_foreignAllowances)
    )

  val def2_foreignEntryMtdJson: JsValue = Json.parse(s"""
      |{
      |   "countryCode":"IND",
      |   "adjustments": $def2_foreignAdjustmentsMtdJson,
      |   "allowances": $def2_foreignAllowancesMtdJson
      |}
      |""".stripMargin)

  val def2_foreignEntryDownstreamJson: JsValue = Json.parse(s"""
     |{
     |   "countryCode":"IND",
     |   "adjustments": $def2_foreignAdjustmentsDownstreamJson,
     |   "allowances": $def2_foreignAllowancesDownstreamJson
     |}
     |""".stripMargin)

  val def2_createAmendForeignPropertyAnnualSubmissionRequestBody: Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
      foreignProperty = List(def2_foreignEntry)
    )

  val def2_createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson: JsValue = Json.parse(s"""
      |{
      |   "foreignProperty":[ $def2_foreignEntryMtdJson ]
      |}
      |""".stripMargin)

  val def2_createAmendForeignPropertyAnnualSubmissionRequestBodyDownstreamJson: JsValue = Json.parse(s"""
      |{
      |   "foreignProperty":[ $def2_foreignEntryDownstreamJson ]
      |}
      |""".stripMargin)

}
