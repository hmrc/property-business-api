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

package v6.retrieveForeignPropertyAnnualSubmission.def2

import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v6.retrieveForeignPropertyAnnualSubmission.def2.request.Def2_RetrieveForeignPropertyAnnualSubmissionRequestData
import v6.retrieveForeignPropertyAnnualSubmission.model.request.RetrieveForeignPropertyAnnualSubmissionRequestData

class Def2_RetrieveForeignPropertyAnnualSubmissionValidatorSpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val validNino       = "AA123456A"
  private val validBusinessId = "XAIS12345678901"
  private val validTaxYear    = "2025-26"

  private val parsedNino       = Nino(validNino)
  private val parsedBusinessId = BusinessId(validBusinessId)
  private val parsedTaxYear    = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, businessId: String) =
    new Def2_RetrieveForeignPropertyAnnualSubmissionValidator(nino, businessId, validTaxYear)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, validBusinessId).validateAndWrapResult()

        result shouldBe Right(Def2_RetrieveForeignPropertyAnnualSubmissionRequestData(parsedNino, parsedBusinessId, parsedTaxYear))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyAnnualSubmissionRequestData] =
          validator("invalid nino", validBusinessId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an incorrectly formatted businessId" in {
        val result: Either[ErrorWrapper, RetrieveForeignPropertyAnnualSubmissionRequestData] =
          validator(validNino, "invalid business id").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BusinessIdFormatError))
      }

    }
  }

}
