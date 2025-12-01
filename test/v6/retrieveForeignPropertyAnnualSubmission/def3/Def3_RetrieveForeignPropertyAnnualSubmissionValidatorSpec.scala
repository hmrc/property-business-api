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

package v6.retrieveForeignPropertyAnnualSubmission.def3

import common.models.domain.PropertyId
import common.models.errors.PropertyIdFormatError
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v6.retrieveForeignPropertyAnnualSubmission.def3.request.Def3_RetrieveForeignPropertyAnnualSubmissionRequestData

class Def3_RetrieveForeignPropertyAnnualSubmissionValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino: String       = "AA123456A"
  private val validBusinessId: String = "XAIS12345678901"
  private val validTaxYear: String    = "2026-27"
  private val validPropertyId: String = "8e8b8450-dc1b-4360-8109-7067337b42cb"

  private val parsedNino: Nino             = Nino(validNino)
  private val parsedBusinessId: BusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear: TaxYear       = TaxYear.fromMtd(validTaxYear)
  private val parsedPropertyId: PropertyId = PropertyId(validPropertyId)

  private def validator(nino: String = validNino, businessId: String = validBusinessId, propertyId: String = validPropertyId) =
    new Def3_RetrieveForeignPropertyAnnualSubmissionValidator(nino, businessId, validTaxYear, Some(propertyId))

  "validator" should {
    "return the parsed domain object" when {
      "a valid request is supplied" in {
        validator().validateAndWrapResult() shouldBe Right(
          Def3_RetrieveForeignPropertyAnnualSubmissionRequestData(
            parsedNino,
            parsedBusinessId,
            parsedTaxYear,
            Some(parsedPropertyId)
          )
        )
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        validator(nino = "A12344A").validateAndWrapResult() shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return BusinessIdFormatError error" when {
      "an invalid business ID is supplied" in {
        validator(businessId = "XAIS").validateAndWrapResult() shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }
    }

    "return PropertyIdFormatError error" when {
      "an invalid property ID is supplied" in {
        validator(propertyId = "8e8b8450").validateAndWrapResult() shouldBe Left(ErrorWrapper(correlationId, PropertyIdFormatError))
      }
    }
  }

}
