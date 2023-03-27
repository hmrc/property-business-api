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

package v2.controllers.requestParsers

import api.mocks.validators.MockRetrieveUkPropertyAnnualSubmissionValidator
import support.UnitSpec
import v2.models.domain.TaxYear; import api.models.domain.Nino
import api.models.errors.{ BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError }
import v2.models.request.retrieveUkPropertyAnnualSubmission._

class RetrieveUkPropertyAnnualSubmissionRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val businessId: String             = "XAIS12345678901"
  val taxYear: String                = "2021-22"
  implicit val correlationId: String = "X-123"

  val inputData: RetrieveUkPropertyAnnualSubmissionRawData =
    RetrieveUkPropertyAnnualSubmissionRawData(nino, businessId, taxYear)

  trait Test extends MockRetrieveUkPropertyAnnualSubmissionValidator {
    lazy val parser = new RetrieveUkPropertyAnnualSubmissionRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveUkPropertyAnnualSubmissionValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(RetrieveUkPropertyAnnualSubmissionRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear)))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveUkPropertyAnnualSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockRetrieveUkPropertyAnnualSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
