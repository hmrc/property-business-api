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

package v6.retrieveForeignPropertyCumulativeSummary.def2

import common.models.domain.PropertyId
import common.models.errors.PropertyIdFormatError
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v6.retrieveForeignPropertyCumulativeSummary.def2.model.request.Def2_RetrieveForeignPropertyCumulativeSummaryRequestData

class Def2_RetrieveForeignPropertyCumulativeSummaryValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2025-26"
  private val validPropertyId = "8e8b8450-dc1b-4360-8109-7067337b42cb"

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)
  private val parsedPropertyId = PropertyId(validPropertyId)

  private def validator(nino: String = validNino,
                        businessId: String = validBusinessId,
                        taxYear: String = validTaxYear,
                        propertyId: String = validPropertyId) =
    new Def2_RetrieveForeignPropertyCumulativeSummaryValidator(nino, businessId, taxYear, Some(propertyId))

  "validator" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        validator().validateAndWrapResult() shouldBe
          Right(Def2_RetrieveForeignPropertyCumulativeSummaryRequestData(parsedNino, parsedBusinessId, parsedTaxYear, Some(parsedPropertyId)))
      }
    }

    "return a single error" when {
      "given an invalid nino" in {
        validator(nino = "invalidNino").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given an invalid business ID" in {
        validator(businessId = "invalidBusinessId").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

      "given an invalid property ID" in {
        validator(propertyId = "invalidPropertyId").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, PropertyIdFormatError))
      }

    }
  }

}
