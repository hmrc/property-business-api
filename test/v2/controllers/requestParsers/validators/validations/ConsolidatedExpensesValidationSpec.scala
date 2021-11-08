/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.controllers.requestParsers.validators.validations

import support.UnitSpec
import v2.models.errors.RuleBothExpensesSuppliedError
import v2.models.request.common
import v2.models.request.common.foreignPropertyEntry.ForeignPropertyExpenditure
import v2.models.request.common.ukPropertyRentARoom.UkPropertyExpensesRentARoom
import v2.models.request.createUkPropertyPeriodSummary.ukFhlProperty.UkFhlPropertyExpenses
import v2.models.request.createUkPropertyPeriodSummary.ukNonFhlProperty.UkNonFhlPropertyExpenses

class ConsolidatedExpensesValidationSpec extends UnitSpec {

  "validate" when {
    val path  = "path"
    val error = RuleBothExpensesSuppliedError.copy(paths = Some(Seq(path)))

    "passed a foreignPropertyEntry model" should {
      val model: ForeignPropertyExpenditure =
        ForeignPropertyExpenditure(None, None, None, None, None, None, None, None, None, Some(123.45))

      "return no errors" when {
        "a valid consolidatedExpenses model is supplied with only consolidatedExpenses" in {
          ConsolidatedExpensesValidation.validate(model, path) shouldBe Nil
        }
        "a valid consolidatedExpenses model is supplied with all consolidatedExpenses fields" in {
          ConsolidatedExpensesValidation.validate(model.copy(residentialFinancialCost = Some(123.45),
                                                             broughtFwdResidentialFinancialCost = Some(123.45)),
                                                  path) shouldBe Nil
        }
      }

      "return an error for amend" when {
        "a model with consolidatedExpenses and premisesRunningCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(premisesRunningCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and repairsAndMaintenance is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(repairsAndMaintenance = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and financialCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(financialCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and professionalFees is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(professionalFees = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and costsOfServices is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(costsOfServices = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and travelCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(travelCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and other is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(other = Some(123.45)), path) shouldBe List(error)
        }
      }
    }

    "passed a foreignFhlEea model" should {
      val model: common.foreignFhlEea.ForeignFhlEeaExpenditure =
        common.foreignFhlEea.ForeignFhlEeaExpenditure(None, None, None, None, None, None, None, Some(123.45))

      "return no errors" when {
        "a valid consolidatedExpenses model is supplied with only consolidatedExpenses" in {
          ConsolidatedExpensesValidation.validate(model, path) shouldBe Nil
        }
      }
      "return an error" when {
        "a model with consolidatedExpenses and premisesRunningCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(premisesRunningCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and repairsAndMaintenance is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(repairsAndMaintenance = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and financialCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(financialCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and professionalFees is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(professionalFees = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and costsOfServices is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(costsOfServices = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and travelCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(travelCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and other is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(other = Some(123.45)), path) shouldBe List(error)
        }
      }
    }

    "passed a uk fhl model" should {
      val model: UkFhlPropertyExpenses =
        UkFhlPropertyExpenses(None, None, None, None, None, None, Some(123.45), None, None)

      "return no errors" when {
        "a valid consolidatedExpenses model is supplied with only consolidatedExpenses" in {
          ConsolidatedExpensesValidation.validate(model, path) shouldBe Nil
        }
      }

      "return an error for amend" when {
        "a model with consolidatedExpenses and premisesRunningCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(premisesRunningCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and repairsAndMaintenance is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(repairsAndMaintenance = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and financialCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(financialCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and professionalFees is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(professionalFees = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and costOfServices is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(costOfServices = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and other is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(other = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and travelCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(travelCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and rentARoom is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(rentARoom = Some(UkPropertyExpensesRentARoom(Some(12.34)))), path) shouldBe List(error)
        }
      }
    }

    "passed a uk non-fhl model" should {
      val model: UkNonFhlPropertyExpenses =
        UkNonFhlPropertyExpenses(None, None, None, None, None, None, None, None, None, None, Some(123.45))

      "return no errors" when {
        "a valid consolidatedExpenses model is supplied with only consolidatedExpenses" in {
          ConsolidatedExpensesValidation.validate(model, path) shouldBe Nil
        }
      }

      "return an error for amend" when {
        "a model with consolidatedExpenses and premisesRunningCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(premisesRunningCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and repairsAndMaintenance is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(repairsAndMaintenance = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and financialCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(financialCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and professionalFees is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(professionalFees = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and costOfServices is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(costOfServices = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and other is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(other = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and residentialFinancialCost is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(residentialFinancialCost = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and residentialFinancialCostsCarriedForward is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(residentialFinancialCostsCarriedForward = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and travelCosts is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(travelCosts = Some(123.45)), path) shouldBe List(error)
        }
        "a model with consolidatedExpenses and rentARoom is supplied" in {
          ConsolidatedExpensesValidation.validate(model.copy(rentARoom = Some(UkPropertyExpensesRentARoom(Some(12.34)))), path) shouldBe List(error)
        }
      }
    }
  }
}
