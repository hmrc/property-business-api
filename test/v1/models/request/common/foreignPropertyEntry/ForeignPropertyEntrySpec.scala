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

package v1.models.request.common.foreignPropertyEntry

import play.api.libs.json.{ JsValue, Json }
import support.UnitSpec

class ForeignPropertyEntrySpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
       |{
       |  "countryCode": "zzz",
       |  "income": {
       |    "rentIncome": {
       |      "rentAmount": 34456.30
       |    },
       |    "foreignTaxCreditRelief": true,
       |    "premiumOfLeaseGrant": 2543.43,
       |    "otherPropertyIncome": 54325.30,
       |    "foreignTaxTakenOff": 6543.01,
       |    "specialWithholdingTaxOrUKTaxPaid": 643245.00
       |  },
       |  "expenditure": {
       |    "premisesRunningCosts": 5635.43,
       |    "repairsAndMaintenance": 3456.65,
       |    "financialCosts": 34532.21,
       |    "professionalFees": 32465.32,
       |    "costsOfServices": 2567.21,
       |    "travelCosts": 2345.76,
       |    "residentialFinancialCost": 21235.22,
       |    "broughtFwdResidentialFinancialCost": 12556.00,
       |    "other": 2425.11,
       |    "consolidatedExpenses": 352.66
       |  }
       |}
     """.stripMargin
  )

  val model: ForeignPropertyEntry = ForeignPropertyEntry(
    countryCode = "zzz",
    income = Some(
      ForeignPropertyIncome(
        rentIncome = Some(ForeignPropertyRentIncome(rentAmount = Some(34456.30))),
        foreignTaxCreditRelief = true,
        premiumOfLeaseGrant = Some(2543.43),
        otherPropertyIncome = Some(54325.30),
        foreignTaxTakenOff = Some(6543.01),
        specialWithholdingTaxOrUKTaxPaid = Some(643245.00)
      )),
    expenditure = Some(
      ForeignPropertyExpenditure(
        premisesRunningCosts = Some(5635.43),
        repairsAndMaintenance = Some(3456.65),
        financialCosts = Some(34532.21),
        professionalFees = Some(32465.32),
        costsOfServices = Some(2567.21),
        travelCosts = Some(2345.76),
        residentialFinancialCost = Some(21235.22),
        broughtFwdResidentialFinancialCost = Some(12556.00),
        other = Some(2425.11),
        consolidatedExpenses = Some(352.66)
      ))
  )

  val ifsJson: JsValue = Json.parse(
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
       |    "foreignTaxPaidOrDeducted": 6543.01,
       |    "specialWithholdingTaxOrUkTaxPaid": 643245.00
       |  },
       |  "expenses": {
       |    "premisesRunningCosts": 5635.43,
       |    "repairsAndMaintenance": 3456.65,
       |    "financialCosts": 34532.21,
       |    "professionalFees": 32465.32,
       |    "costOfServices": 2567.21,
       |    "travelCosts": 2345.76,
       |    "residentialFinancialCost": 21235.22,
       |    "broughtFwdResidentialFinancialCost": 12556.00,
       |    "other": 2425.11,
       |    "consolidatedExpense": 352.66
       |  }
       |}
     """.stripMargin
  )

  "reads" should {
    "read from JSON" when {
      "valid JSON is provided" in {
        mtdJson.as[ForeignPropertyEntry] shouldBe model
      }
    }
  }

  "writes" should {
    "write to JSON" when {
      "valid model is provided" in {
        Json.toJson(model) shouldBe ifsJson
      }
    }
  }
}
