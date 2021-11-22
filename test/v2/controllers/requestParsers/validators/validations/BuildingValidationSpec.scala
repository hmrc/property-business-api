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
import v2.models.errors.RuleBuildingNameNumberError
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty.Building

class BuildingValidationSpec extends UnitSpec {

  "validate" should {
    "return no errors" when {
      "name is provided" in {
        val building = Building(Some("name"), None, "postcode")
        val validationResult = BuildingValidation.validate(building, "path")
        validationResult.isEmpty shouldBe true
      }
      "number is provided" in {
        val building = Building(None, Some("number"), "postcode")
        val validationResult = BuildingValidation.validate(building, "path")
        validationResult.isEmpty shouldBe true
      }
      "name and number is provided" in {
        val building = Building(Some("name"), Some("number"), "postcode")
        val validationResult = BuildingValidation.validate(building, "path")
        validationResult.isEmpty shouldBe true
      }
    }
    "return RuleBuildingNameNumberError error" when {
      "no name or number is provided" in {
        val building = Building(None, None, "postcode")
        val validationResult = BuildingValidation.validate(building, "path")
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleBuildingNameNumberError.copy(paths = Some(Seq("path")))
      }
    }
  }
}
