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
import v6.createAmendForeignPropertyAnnualSubmission.def1.model.request.def1_foreignProperty.{
  Def1_Create_Amend_Building,
  Def1_Create_Amend_FirstYear,
  Def1_Create_Amend_StructuredBuildingAllowance
}

trait StructuredBuildingAllowanceFixture {

  val structuredBuildingAllowance: Def1_Create_Amend_StructuredBuildingAllowance =
    Def1_Create_Amend_StructuredBuildingAllowance(
      3000.30,
      Some(
        Def1_Create_Amend_FirstYear(
          "2020-01-01",
          3000.40
        )),
      Def1_Create_Amend_Building(
        Some("house name"),
        Some("house number"),
        "GF49JH"
      )
    )

  val structuredBuildingAllowanceMtdJson: JsValue = Json.parse("""
      |{
      |  "amount": 3000.30,
      |  "firstYear": {
      |    "qualifyingDate": "2020-01-01",
      |    "qualifyingAmountExpenditure": 3000.40
      |  },
      |  "building": {
      |    "name": "house name",
      |    "number": "house number",
      |    "postcode": "GF49JH"
      |  }
      |}
      |""".stripMargin)

  val structuredBuildingAllowanceDownstreamJson: JsValue = Json.parse("""
      |{
      |  "amount": 3000.30,
      |  "firstYear": {
      |    "qualifyingDate": "2020-01-01",
      |    "qualifyingAmountExpenditure": 3000.40
      |  },
      |  "building": {
      |    "name": "house name",
      |    "number": "house number",
      |    "postCode": "GF49JH"
      |  }
      |}
      |""".stripMargin)

}
