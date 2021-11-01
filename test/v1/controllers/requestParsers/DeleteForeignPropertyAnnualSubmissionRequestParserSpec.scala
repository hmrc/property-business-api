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

package v1.controllers.requestParsers

import support.UnitSpec
import v1.mocks.validators.MockDeleteForeignPropertyAnnualSubmissionValidator
import v1.models.domain.Nino
import v1.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import v1.models.request.deleteForeignPropertyAnnualSubmission._

class DeleteForeignPropertyAnnualSubmissionRequestParserSpec extends UnitSpec {

  val nino: String = "AA123456B"
  val businessId: String = "XAIS12345678901"
  val taxYear: String = "2021-22"
  implicit val correlationId: String = "X-123"

  val inputData: DeleteForeignPropertyAnnualSubmissionRawData =
    DeleteForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear)

  trait Test extends MockDeleteForeignPropertyAnnualSubmissionValidator {
    lazy val parser = new DeleteForeignPropertyAnnualSubmissionRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockDeleteForeignPropertyAnnualSubmissionValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(DeleteForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear))
      }
    }
    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockDeleteForeignPropertyAnnualSubmissionValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
      "multiple validation errors occur" in new Test {
        MockDeleteForeignPropertyAnnualSubmissionValidator.validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
