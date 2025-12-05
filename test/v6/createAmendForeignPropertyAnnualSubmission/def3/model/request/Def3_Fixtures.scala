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

package v6.createAmendForeignPropertyAnnualSubmission.def3.model.request

import play.api.libs.json.{JsValue, Json}
import v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.def3_foreignProperty.{
  Def3_Create_Amend_ForeignAdjustments,
  Def3_Create_Amend_ForeignAllowances,
  Def3_Create_Amend_ForeignEntry
}

trait Def3_Fixtures extends Def3_StructuredBuildingAllowanceFixture {

  val def3_foreignAdjustments: Def3_Create_Amend_ForeignAdjustments =
    Def3_Create_Amend_ForeignAdjustments(
      privateUseAdjustment = Some(1.25),
      balancingCharge = Some(2.25)
    )

  val def3_foreignAdjustmentsMtdJson: JsValue = Json.parse("""
      |{
      |    "privateUseAdjustment":1.25,
      |    "balancingCharge":2.25
      |}
      |""".stripMargin)

  val def3_foreignAdjustmentsDownstreamJson: JsValue = def3_foreignAdjustmentsMtdJson

  val def3_foreignAllowances: Def3_Create_Amend_ForeignAllowances =
    Def3_Create_Amend_ForeignAllowances(
      annualInvestmentAllowance = Some(1.25),
      costOfReplacingDomesticItems = Some(2.25),
      otherCapitalAllowance = Some(4.25),
      zeroEmissionsCarAllowance = Some(6.25),
      propertyIncomeAllowance = None,
      structuredBuildingAllowance = Some(List(def3_structuredBuildingAllowance))
    )

  val def3_foreignAllowancesMtdJson: JsValue = Json.parse(s"""
      |{
      |    "annualInvestmentAllowance":1.25,
      |    "costOfReplacingDomesticItems":2.25,
      |    "otherCapitalAllowance":4.25,
      |    "zeroEmissionsCarAllowance":6.25,
      |    "structuredBuildingAllowance": [$def3_structuredBuildingAllowanceMtdJson]
      |}
      |""".stripMargin)

  val def3_foreignAllowancesDownstreamJson: JsValue = Json.parse(s"""
      |{
      |    "annualInvestmentAllowance":1.25,
      |    "costOfReplacingDomesticItems":2.25,
      |    "otherCapitalAllowance":4.25,
      |    "zeroEmissionsCarAllowance":6.25,
      |    "structuredBuildingAllowance": [$def3_structuredBuildingAllowanceDownstreamJson]
      |}
      |""".stripMargin)

  val def3_foreignEntry: Def3_Create_Amend_ForeignEntry =
    Def3_Create_Amend_ForeignEntry(
      propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb",
      adjustments = Some(def3_foreignAdjustments),
      allowances = Some(def3_foreignAllowances)
    )

  val def3_foreignEntryMtdJson: JsValue = Json.parse(s"""
      |{
      |   "propertyId":"8e8b8450-dc1b-4360-8109-7067337b42cb",
      |   "adjustments": $def3_foreignAdjustmentsMtdJson,
      |   "allowances": $def3_foreignAllowancesMtdJson
      |}
      |""".stripMargin)

  val def3_foreignEntryDownstreamJson: JsValue = Json.parse(s"""
     |{
     |   "propertyId":"8e8b8450-dc1b-4360-8109-7067337b42cb",
     |   "adjustments": $def3_foreignAdjustmentsDownstreamJson,
     |   "allowances": $def3_foreignAllowancesDownstreamJson
     |}
     |""".stripMargin)

  val def3_createAmendForeignPropertyAnnualSubmissionRequestBody: Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
      foreignProperty = List(def3_foreignEntry)
    )

  val def3_createAmendForeignPropertyAnnualSubmissionRequestBodyMtdJson: JsValue = Json.parse(s"""
      |{
      |   "foreignProperty":[ $def3_foreignEntryMtdJson ]
      |}
      |""".stripMargin)

  val def3_createAmendForeignPropertyAnnualSubmissionRequestBodyDownstreamJson: JsValue = Json.parse(s"""
      |{
      |   "foreignProperty":[ $def3_foreignEntryDownstreamJson ]
      |}
      |""".stripMargin)

  val def3_foreignEntryPropertyIncomeAllowanceRequestBody: Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
      List(
        Def3_Create_Amend_ForeignEntry(
          "8e8b8450-dc1b-4360-8109-7067337b42cb",
          Some(Def3_Create_Amend_ForeignAdjustments(None, Some(3453.34))),
          Some(
            Def3_Create_Amend_ForeignAllowances(
              None,
              None,
              None,
              None,
              Some(100.95),
              None
            ))
        ))
    )

  val def3_propertyIncomeAllowanceRequestBodyMtdJson: JsValue = Json.parse("""
      |{
      |   "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
      |   "adjustments": {
      |      "balancingCharge": 3453.34
      |   },
      |   "allowances": {
      |      "propertyIncomeAllowance": 100.95
      |   }
      |}
      |""".stripMargin)

  val def3_minimalForeignPropertyRequestBody: Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
      List(
        Def3_Create_Amend_ForeignEntry(
          "8e8b8450-dc1b-4360-8109-7067337b42cb",
          Some(
            Def3_Create_Amend_ForeignAdjustments(
              None,
              Some(12.34)
            )),
          None
        )
      )
    )

  val def3_minimalForeignOnlyAllowancesRequestBody: Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody =
    Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody(
      List(
        Def3_Create_Amend_ForeignEntry(
          "8e8b8450-dc1b-4360-8109-7067337b42cb",
          None,
          Some(
            Def3_Create_Amend_ForeignAllowances(
              Some(38330.95),
              None,
              None,
              None,
              None,
              None
            )
          )
        )
      )
    )

}
