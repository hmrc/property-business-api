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
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea.ForeignFhlEeaAllowances
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignNonFhl.ForeignNonFhlAllowances
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty.UkFhlPropertyAllowances
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty.UkNonFhlPropertyAllowances
import v2.models.request.common.{ Building, StructuredBuildingAllowance }

class AllowancesValidationSpec extends UnitSpec {

  "validate (UK Fhl)" should {
    "return no errors" when {
      "only propertyIncomeAllowance is provided" in {
        val allowances       = UkFhlPropertyAllowances(None, None, None, None, None, Some(76543.73))
        val validationResult = AllowancesValidation.validateUkFhl(allowances, "path")
        validationResult shouldBe Nil
      }
      "everything apart from propertyIncomeAllowance is provided" in {
        val allowances       = UkFhlPropertyAllowances(Some(2576.26), Some(3645.36), Some(254.66), Some(827.33), Some(909.11), None)
        val validationResult = AllowancesValidation.validateUkFhl(allowances, "path")
        validationResult shouldBe Nil
      }
      "some fields excluding propertyIncomeAllowance is provided" in {
        val allowances       = UkFhlPropertyAllowances(Some(2576.26), None, None, None, Some(909.11), None)
        val validationResult = AllowancesValidation.validateUkFhl(allowances, "path")
        validationResult shouldBe Nil
      }
    }
    "return RuleBothAllowancesSuppliedError error" when {
      "propertyIncomeAllowance is provided with other fields" in {
        val allowances       = UkFhlPropertyAllowances(Some(2576.26), None, None, None, Some(909.11), Some(76543.73))
        val validationResult = AllowancesValidation.validateUkFhl(allowances, "path")
        validationResult shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }
    }
  }

  "validate (UK NonFhl)" should {
    "return no errors" when {
      "only propertyIncomeAllowance is provided" in {
        val allowances       = UkNonFhlPropertyAllowances(None, None, None, None, None, None, None, Some(5326.11), None, None)
        val validationResult = AllowancesValidation.validateUkNonFhl(allowances, "path")
        validationResult shouldBe Nil
      }
      "everything apart from propertyIncomeAllowance is provided" in {
        val structuredBuildingAllowance         = StructuredBuildingAllowance(514.34, None, Building(Some("name"), Some("number"), "postcode"))
        val enhancedStructuredBuildingAllowance = StructuredBuildingAllowance(514.34, None, Building(Some("name"), Some("number"), "postcode"))
        val allowances = UkNonFhlPropertyAllowances(Some(5326.11), Some(5326.11), Some(5326.11), Some(5326.11), Some(5326.11), Some(5326.11),
          Some(5326.11), None, Some(Seq(structuredBuildingAllowance)), Some(Seq(enhancedStructuredBuildingAllowance)))
        val validationResult = AllowancesValidation.validateUkNonFhl(allowances, "path")
        validationResult shouldBe Nil
      }
      "some fields excluding propertyIncomeAllowance is provided" in {
        val allowances       = UkNonFhlPropertyAllowances(Some(5326.11), None, Some(5326.11), None, None, None, None, None, None, None)
        val validationResult = AllowancesValidation.validateUkNonFhl(allowances, "path")
        validationResult shouldBe Nil
      }
    }
    "return RuleBothAllowancesSuppliedError error" when {
      "propertyIncomeAllowance is provided with other fields" in {
        val allowances       = UkNonFhlPropertyAllowances(Some(5326.11), None, Some(5326.11), None, None, Some(5326.11), None, Some(5326.11), None, None)
        val validationResult = AllowancesValidation.validateUkNonFhl(allowances, "path")
        validationResult shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }
    }
  }

  "validate (Foreign Fhl)" should {

    val provided: Option[BigDecimal] = Some(1)

    "return no errors" when {
      "other fields are provided without propertyIncomeAllowance" in {
        val allowances = ForeignFhlEeaAllowances(provided, provided, provided, provided, propertyIncomeAllowance = None)
        AllowancesValidation.validateForeignFhl(allowances, "path") shouldBe Nil
      }

      "only propertyIncomeAllowance is provided" in {
        val allowances = ForeignFhlEeaAllowances(None, None, None, None, propertyIncomeAllowance = provided)
        AllowancesValidation.validateForeignFhl(allowances, "path") shouldBe Nil
      }
    }

    "return an error" when {
      "annualInvestmentAllowance is provided with propertyIncomeAllowance" in {
        val allowances = ForeignFhlEeaAllowances(annualInvestmentAllowance = provided, None, None, None, propertyIncomeAllowance = provided)
        AllowancesValidation.validateForeignFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }

      "otherCapitalAllowance is provided with propertyIncomeAllowance" in {
        val allowances = ForeignFhlEeaAllowances(None, otherCapitalAllowance = provided, None, None, propertyIncomeAllowance = provided)
        AllowancesValidation.validateForeignFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }

      "electricChargePointAllowance is provided with propertyIncomeAllowance" in {
        val allowances = ForeignFhlEeaAllowances(None, None, electricChargePointAllowance = provided, None, propertyIncomeAllowance = provided)
        AllowancesValidation.validateForeignFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }

      "zeroEmissionsCarAllowance is provided with propertyIncomeAllowance" in {
        val allowances = ForeignFhlEeaAllowances(None, None, None, zeroEmissionsCarAllowance = provided, propertyIncomeAllowance = provided)
        AllowancesValidation.validateForeignFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }
    }
  }

  "validate (Foreign NonFhl)" should {
    val provided: Option[BigDecimal]   = Some(1)
    val structuredBuildingAllowance = Some(List(StructuredBuildingAllowance(1, None, Building(None, None, "X1A 1XX"))))

    "return no errors" when {
      "other fields are provided without propertyIncomeAllowance" in {
        val allowances =
          ForeignNonFhlAllowances(provided, provided, provided, provided, provided, provided, propertyIncomeAllowance = None, structuredBuildingAllowance)
        AllowancesValidation.validateForeignNonFhl(allowances, "path") shouldBe Nil
      }

      "only propertyIncomeAllowance is provided" in {
        val allowances = ForeignNonFhlAllowances(None, None, None, None, None, None, propertyIncomeAllowance = None, None)
        AllowancesValidation.validateForeignNonFhl(allowances, "path") shouldBe Nil
      }
    }

    "return an error" when {
      "annualInvestmentAllowance is provided with propertyIncomeAllowance" in {
        val allowances =
          ForeignNonFhlAllowances(annualInvestmentAllowance = provided, None, None, None, None, None, propertyIncomeAllowance = provided, None)
        AllowancesValidation.validateForeignNonFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }

      "costOfReplacingDomesticItems is provided with propertyIncomeAllowance" in {
        val allowances =
          ForeignNonFhlAllowances(None, costOfReplacingDomesticItems = provided, None, None, None, None, propertyIncomeAllowance = provided, None)
        AllowancesValidation.validateForeignNonFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }

      "zeroEmissionsGoodsVehicleAllowance is provided with propertyIncomeAllowance" in {
        val allowances =
          ForeignNonFhlAllowances(None, None, zeroEmissionsGoodsVehicleAllowance = provided, None, None, None, propertyIncomeAllowance = provided, None)
        AllowancesValidation.validateForeignNonFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }

      "otherCapitalAllowance is provided with propertyIncomeAllowance" in {
        val allowances = ForeignNonFhlAllowances(None, None, None, otherCapitalAllowance = provided, None, None, propertyIncomeAllowance = provided, None)
        AllowancesValidation.validateForeignNonFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }

      "electricChargePointAllowance is provided with propertyIncomeAllowance" in {
        val allowances =
          ForeignNonFhlAllowances(None, None, None, None, electricChargePointAllowance = provided, None, propertyIncomeAllowance = provided, None)
        AllowancesValidation.validateForeignNonFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }

      "zeroEmissionsCarAllowance is provided with propertyIncomeAllowance" in {
        val allowances =
          ForeignNonFhlAllowances(None, None, None, None, None, zeroEmissionsCarAllowance = provided, propertyIncomeAllowance = provided, None)
        AllowancesValidation.validateForeignNonFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }

      "structuredBuildingAllowance is provided with propertyIncomeAllowance" in {
        val allowances =
          ForeignNonFhlAllowances(None, None, None, None, None, None, propertyIncomeAllowance = provided, structuredBuildingAllowance)
        AllowancesValidation.validateForeignNonFhl(allowances, "path") shouldBe List(RuleBothAllowancesSuppliedError.copy(paths = Some(Seq("path"))))
      }
    }
  }
}
