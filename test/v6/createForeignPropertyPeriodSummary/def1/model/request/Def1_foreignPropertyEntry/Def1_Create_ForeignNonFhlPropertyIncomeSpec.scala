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

package v6.createForeignPropertyPeriodSummary.def1.model.request.Def1_foreignPropertyEntry

import play.api.libs.json.Json
import shared.utils.UnitSpec

class Def1_Create_ForeignNonFhlPropertyIncomeSpec extends UnitSpec {

  private val mtdJson = Json.parse(
    """
      |{
      |  "rentIncome": {
      |    "rentAmount": 34456.30
      |  },
      |  "foreignTaxCreditRelief": true,
      |  "premiumsOfLeaseGrant": 2543.43,
      |  "otherPropertyIncome": 54325.30,
      |  "foreignTaxPaidOrDeducted": 6543.01,
      |  "specialWithholdingTaxOrUkTaxPaid": 643245.00
      |}
    """.stripMargin
  )

  private val model = Def1_Create_ForeignNonFhlPropertyIncome(
    rentIncome = Some(Def1_Create_ForeignNonFhlPropertyRentIncome(rentAmount = Some(34456.30))),
    foreignTaxCreditRelief = true,
    premiumsOfLeaseGrant = Some(2543.43),
    otherPropertyIncome = Some(54325.30),
    foreignTaxPaidOrDeducted = Some(6543.01),
    specialWithholdingTaxOrUkTaxPaid = Some(643245.00)
  )

  private val downstreamJson = Json.parse(
    """
      |{
      |  "rentIncome": {
      |    "rentAmount": 34456.30
      |  },
      |  "foreignTaxCreditRelief": true,
      |  "premiumsOfLeaseGrant": 2543.43,
      |  "otherPropertyIncome": 54325.30,
      |  "foreignTaxPaidOrDeducted": 6543.01,
      |  "specialWithholdingTaxOrUkTaxPaid": 643245.00
      |}
    """.stripMargin
  )

  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[Def1_Create_ForeignNonFhlPropertyIncome] shouldBe model
      }
    }
  }

  "writes" should {
    "write to JSON" when {
      "valid model is provided" in {
        Json.toJson(model) shouldBe downstreamJson
      }
    }
  }

}
