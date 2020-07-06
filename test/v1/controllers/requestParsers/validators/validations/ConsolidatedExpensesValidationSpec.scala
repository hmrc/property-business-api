/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.RuleBothExpensesSuppliedError
import v1.models.request.amend._

class ConsolidatedExpensesValidationSpec extends UnitSpec {

  "validate" when {
    "passed a foreignProperty model" should {
      val consolidatedExpensesModel: foreignPropertyEntry.Expenditure =
        foreignPropertyEntry.Expenditure(None, None, None, None, None, None, None, None, None, Some(123.45))

      "return no errors" when {
        "a valid consolidatedExpenses model is supplied with only consolidatedExpenses" in {
          val model = consolidatedExpensesModel
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe Nil
        }
        "a valid consolidatedExpenses model is supplied with all consolidatedExpenses fields" in {
          val model = consolidatedExpensesModel.copy(residentialFinancialCost = Some(123.45), broughtFwdResidentialFinancialCost = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe Nil
        }
      }
      "return an error" when {
        "a model with consolidatedExpenses and premisesRunningCosts is supplied" in {
          val model = consolidatedExpensesModel.copy(premisesRunningCosts = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and repairsAndMaintenance is supplied" in {
          val model = consolidatedExpensesModel.copy(repairsAndMaintenance = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and financialCosts is supplied" in {
          val model = consolidatedExpensesModel.copy(financialCosts = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and professionalFees is supplied" in {
          val model = consolidatedExpensesModel.copy(professionalFees = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and costsOfServices is supplied" in {
          val model = consolidatedExpensesModel.copy(costsOfServices = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and travelCosts is supplied" in {
          val model = consolidatedExpensesModel.copy(travelCosts = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and other is supplied" in {
          val model = consolidatedExpensesModel.copy(other = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
      }
    }
    "passed a foreignFhlEea model" should {
      val consolidatedExpensesModel: foreignFhlEea.Expenditure =
        foreignFhlEea.Expenditure(None, None, None, None, None, None, None, Some(123.45))

      "return no errors" when {
        "a valid consolidatedExpenses model is supplied with only consolidatedExpenses" in {
          val model = consolidatedExpensesModel
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe Nil
        }
      }
      "return an error" when {
        "a model with consolidatedExpenses and premisesRunningCosts is supplied" in {
          val model = consolidatedExpensesModel.copy(premisesRunningCosts = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and repairsAndMaintenance is supplied" in {
          val model = consolidatedExpensesModel.copy(repairsAndMaintenance = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and financialCosts is supplied" in {
          val model = consolidatedExpensesModel.copy(financialCosts = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and professionalFees is supplied" in {
          val model = consolidatedExpensesModel.copy(professionalFees = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and costsOfServices is supplied" in {
          val model = consolidatedExpensesModel.copy(costsOfServices = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and travelCosts is supplied" in {
          val model = consolidatedExpensesModel.copy(travelCosts = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
        "a model with consolidatedExpenses and other is supplied" in {
          val model = consolidatedExpensesModel.copy(other = Some(123.45))
          ConsolidatedExpensesValidation.validate(model, "path") shouldBe List(RuleBothExpensesSuppliedError.copy(paths = Some(Seq("path"))))
        }
      }
    }
  }
}
