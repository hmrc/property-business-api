/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveForeignPropertyAnnualSubmission.def3.fixture

import play.api.libs.json.{JsValue, Json}
import shared.models.domain.Timestamp
import v6.retrieveForeignPropertyAnnualSubmission.def3.model.response.*

object Def3_RetrieveForeignPropertyAnnualSubmissionFixture {

  val retrieveBuilding: RetrieveBuilding = RetrieveBuilding(
    name = Some("Building Name"),
    number = Some("12"),
    postcode = "TF3 4GH"
  )

  val retrieveFirstYear: RetrieveFirstYear = RetrieveFirstYear(
    qualifyingDate = "2020-03-29",
    qualifyingAmountExpenditure = 100.25
  )

  val retrieveForeignPropertyAdjustments: RetrieveForeignPropertyAdjustments = RetrieveForeignPropertyAdjustments(
    privateUseAdjustment = Some(100.25),
    balancingCharge = Some(100.25)
  )

  val retrieveStructuredBuildingAllowance: RetrieveStructuredBuildingAllowance = RetrieveStructuredBuildingAllowance(
    amount = 100.25,
    firstYear = Some(retrieveFirstYear),
    building = retrieveBuilding
  )

  val retrieveForeignPropertyAllowances: RetrieveForeignPropertyAllowances = RetrieveForeignPropertyAllowances(
    annualInvestmentAllowance = Some(100.25),
    costOfReplacingDomesticItems = Some(100.25),
    otherCapitalAllowance = Some(100.25),
    zeroEmissionsCarAllowance = Some(100.25),
    propertyIncomeAllowance = Some(100.25),
    structuredBuildingAllowance = Some(List(retrieveStructuredBuildingAllowance))
  )

  val retrieveForeignPropertyEntry: RetrieveForeignPropertyEntry = RetrieveForeignPropertyEntry(
    propertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb",
    adjustments = Some(retrieveForeignPropertyAdjustments),
    allowances = Some(retrieveForeignPropertyAllowances)
  )

  val fullResponseModel: Def3_RetrieveForeignPropertyAnnualSubmissionResponse = Def3_RetrieveForeignPropertyAnnualSubmissionResponse(
    submittedOn = Timestamp("2026-11-06T10:36:54.683Z"),
    foreignProperty = List(retrieveForeignPropertyEntry)
  )

  val retrieveBuildingMtdJson: JsValue = Json.parse(
    """
      |{
      |  "name": "Building Name",
      |  "number": "12",
      |  "postcode": "TF3 4GH"
      |}
    """.stripMargin
  )

  val retrieveFirstYearJson: JsValue = Json.parse(
    """
      |{
      |  "qualifyingDate": "2020-03-29",
      |  "qualifyingAmountExpenditure": 100.25
      |}
    """.stripMargin
  )

  val retrieveForeignPropertyAdjustmentsJson: JsValue = Json.parse(
    """
      |{
      |  "privateUseAdjustment": 100.25,
      |  "balancingCharge": 100.25
      |}
    """.stripMargin
  )

  val retrieveStructuredBuildingAllowanceMtdJson: JsValue = Json.parse(
    s"""
      |{
      |  "amount": 100.25,
      |  "firstYear": $retrieveFirstYearJson,
      |  "building": $retrieveBuildingMtdJson
      |}
    """.stripMargin
  )

  val retrieveForeignPropertyAllowancesMtdJson: JsValue = Json.parse(
    s"""
      |{
      |  "annualInvestmentAllowance": 100.25,
      |  "costOfReplacingDomesticItems": 100.25,
      |  "otherCapitalAllowance": 100.25,
      |  "zeroEmissionsCarAllowance": 100.25,
      |  "propertyIncomeAllowance": 100.25,
      |  "structuredBuildingAllowance": [$retrieveStructuredBuildingAllowanceMtdJson]
      |}
    """.stripMargin
  )

  val retrieveForeignPropertyEntryMtdJson: JsValue = Json.parse(
    s"""
      |{
      |  "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
      |  "adjustments": $retrieveForeignPropertyAdjustmentsJson,
      |  "allowances": $retrieveForeignPropertyAllowancesMtdJson
      |}
    """.stripMargin
  )

  val fullResponseMtdJson: JsValue = Json.parse(
    s"""
      |{
      |  "submittedOn": "2026-11-06T10:36:54.683Z",
      |  "foreignProperty": [$retrieveForeignPropertyEntryMtdJson]
      |}
    """.stripMargin
  )

  val retrieveBuildingDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "name": "Building Name",
      |  "number": "12",
      |  "postCode": "TF3 4GH"
      |}
    """.stripMargin
  )

  val retrieveStructuredBuildingAllowanceDownstreamJson: JsValue = Json.parse(
    s"""
      |{
      |  "amount": 100.25,
      |  "firstYear": $retrieveFirstYearJson,
      |  "building": $retrieveBuildingDownstreamJson
      |}
    """.stripMargin
  )

  val retrieveForeignPropertyAllowancesDownstreamJson: JsValue = Json.parse(
    s"""
      |{
      |  "annualInvestmentAllowance": 100.25,
      |  "costOfReplacingDomesticItems": 100.25,
      |  "otherCapitalAllowance": 100.25,
      |  "zeroEmissionsCarAllowance": 100.25,
      |  "propertyAllowance": 100.25,
      |  "structuredBuildingAllowance": [$retrieveStructuredBuildingAllowanceDownstreamJson]
      |}
    """.stripMargin
  )

  val retrieveForeignPropertyEntryDownstreamJson: JsValue = Json.parse(
    s"""
      |{
      |  "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb",
      |  "adjustments": $retrieveForeignPropertyAdjustmentsJson,
      |  "allowances": $retrieveForeignPropertyAllowancesDownstreamJson
      |}
    """.stripMargin
  )

  val fullResponseDownstreamJson: JsValue = Json.parse(
    s"""
      |{
      |  "submittedOn": "2026-11-06T10:36:54.683Z",
      |  "foreignProperty": [$retrieveForeignPropertyEntryDownstreamJson]
      |}
    """.stripMargin
  )

}
