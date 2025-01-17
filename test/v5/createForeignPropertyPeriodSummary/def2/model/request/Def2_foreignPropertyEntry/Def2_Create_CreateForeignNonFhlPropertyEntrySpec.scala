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

package v5.createForeignPropertyPeriodSummary.def2.model.request.Def2_foreignPropertyEntry

import play.api.libs.json.Json
import shared.utils.UnitSpec

class Def2_Create_CreateForeignNonFhlPropertyEntrySpec extends UnitSpec {

  private val mtdJson = Json.parse(
    """
      |{
      |  "countryCode": "zzz",
      |  "income": {
      |    "rentIncome": {
      |      "rentAmount": 34456.30
      |    },
      |    "foreignTaxCreditRelief": true,
      |    "premiumsOfLeaseGrant": 2543.43,
      |    "otherPropertyIncome": 54325.30,
      |    "foreignTaxPaidOrDeducted": 6543.02,
      |    "specialWithholdingTaxOrUkTaxPaid": 643245.00
      |  },
      |  "expenses": {
      |    "premisesRunningCosts": 5635.43,
      |    "repairsAndMaintenance": 3456.65,
      |    "financialCosts": 34532.22,
      |    "professionalFees": 32465.32,
      |    "costOfServices": 2567.22,
      |    "travelCosts": 2345.76,
      |    "residentialFinancialCost": 22235.22,
      |    "broughtFwdResidentialFinancialCost": 22556.00,
      |    "other": 2425.22,
      |    "consolidatedExpenses": 352.66
      |  }
      |}
     """.stripMargin
  )

  private val model = Def2_Create_CreateForeignNonFhlPropertyEntry(
    countryCode = "zzz",
    income = Some(
      Def2_Create_ForeignNonFhlPropertyIncome(
        rentIncome = Some(Def2_Create_ForeignNonFhlPropertyRentIncome(rentAmount = Some(34456.30))),
        foreignTaxCreditRelief = true,
        premiumsOfLeaseGrant = Some(2543.43),
        otherPropertyIncome = Some(54325.30),
        foreignTaxPaidOrDeducted = Some(6543.02),
        specialWithholdingTaxOrUkTaxPaid = Some(643245.00)
      )),
    expenses = Some(
      Def2_Create_CreateForeignNonFhlPropertyExpenses(
        premisesRunningCosts = Some(5635.43),
        repairsAndMaintenance = Some(3456.65),
        financialCosts = Some(34532.22),
        professionalFees = Some(32465.32),
        costOfServices = Some(2567.22),
        travelCosts = Some(2345.76),
        residentialFinancialCost = Some(22235.22),
        broughtFwdResidentialFinancialCost = Some(22556.00),
        other = Some(2425.22),
        consolidatedExpenses = Some(352.66)
      ))
  )

  private val downstreamJson = Json.parse(
    """
      |{
      |  "countryCode": "zzz",
      |  "income": {
      |    "rentIncome": {
      |      "rentAmount": 34456.30
      |    },
      |    "foreignTaxCreditRelief": true,
      |    "premiumsOfLeaseGrant": 2543.43,
      |    "otherPropertyIncome": 54325.30,
      |    "foreignTaxPaidOrDeducted": 6543.02,
      |    "specialWithholdingTaxOrUkTaxPaid": 643245.00
      |  },
      |  "expenses": {
      |    "premisesRunningCosts": 5635.43,
      |    "repairsAndMaintenance": 3456.65,
      |    "financialCosts": 34532.22,
      |    "professionalFees": 32465.32,
      |    "costOfServices": 2567.22,
      |    "travelCosts": 2345.76,
      |    "residentialFinancialCost": 22235.22,
      |    "broughtFwdResidentialFinancialCost": 22556.00,
      |    "other": 2425.22,
      |    "consolidatedExpenseAmount": 352.66
      |  }
      |}
     """.stripMargin
  )

  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[Def2_Create_CreateForeignNonFhlPropertyEntry] shouldBe model
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
