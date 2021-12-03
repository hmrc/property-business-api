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
import v2.models.errors.RuleBothAllowancesSuppliedError
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty.UkFhlPropertyAllowances
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty.UkNonFhlPropertyAllowances
import v2.models.request.common.{Building, StructuredBuildingAllowance}

class AllowancesValidationSpec extends UnitSpec {

  "validate (Fhl)" should {
    "return no errors" when {
      "only consolidatedExpenses is provided" in {
        val allowances = UkFhlPropertyAllowances(None, None, None, None, None, Some(76543.73))
        val validationResult = AllowancesValidation.validate(allowances, "path")
        validationResult.isEmpty shouldBe true
      }
      "everything apart from consolidatedExpenses is provided" in {
        val allowances = UkFhlPropertyAllowances(Some(2576.26), Some(3645.36), Some(254.66), Some(827.33), Some(909.11), None)
        val validationResult = AllowancesValidation.validate(allowances, "path")
        validationResult.isEmpty shouldBe true
      }
      "some fields excluding consolidatedExpenses is provided" in {
        val allowances = UkFhlPropertyAllowances(Some(2576.26), None, None, None, Some(909.11), None)
        val validationResult = AllowancesValidation.validate(allowances, "path")
        validationResult.isEmpty shouldBe true
      }
    }
    "return RuleBothAllowancesSuppliedError error" when {
      "no name or number is provided" in {
        val allowances = UkFhlPropertyAllowances(Some(2576.26), None, None, None, Some(909.11), Some(76543.73))
        val validationResult = AllowancesValidation.validate(allowances, "path")
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path")))
      }
    }
  }

  "validate (NonFhl)" should {
    "return no errors" when {
      "only consolidatedExpenses is provided" in {
        val allowances = UkNonFhlPropertyAllowances(None, None, None, None, None, None, None, Some(5326.11), None, None)
        val validationResult = AllowancesValidation.validate(allowances, "path")
        validationResult.isEmpty shouldBe true
      }
      "everything apart from consolidatedExpenses is provided" in {
        val structuredBuildingAllowance = StructuredBuildingAllowance(514.34, None, Building(Some("name"), Some("number"), "postcode"))
        val enhancedStructuredBuildingAllowance = StructuredBuildingAllowance(514.34, None, Building(Some("name"), Some("number"), "postcode"))
        val allowances = UkNonFhlPropertyAllowances(Some(5326.11), Some(5326.11), Some(5326.11), Some(5326.11), Some(5326.11), Some(5326.11), Some(5326.11), None, Some(Seq(structuredBuildingAllowance)), Some(Seq(enhancedStructuredBuildingAllowance)))
        val validationResult = AllowancesValidation.validate(allowances, "path")
        validationResult.isEmpty shouldBe true
      }
      "some fields excluding consolidatedExpenses is provided" in {
        val allowances = UkNonFhlPropertyAllowances(Some(5326.11), None, Some(5326.11), None, None, None, None, None, None, None)
        val validationResult = AllowancesValidation.validate(allowances, "path")
        validationResult.isEmpty shouldBe true
      }
    }
    "return RuleBothAllowancesSuppliedError error" when {
      "no name or number is provided" in {
        val allowances = UkNonFhlPropertyAllowances(Some(5326.11), None, Some(5326.11), None, None, Some(5326.11), None, Some(5326.11), None, None)
        val validationResult = AllowancesValidation.validate(allowances, "path")
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path")))
      }
    }
  }
}