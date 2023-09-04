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

import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import support.UnitSpec
import v2.mocks.validators.MockDeletePropertyAnnualSubmissionValidator
import v2.models.request.deletePropertyAnnualSubmission._

class DeletePropertyAnnualSubmissionRequestDataParserSpec extends UnitSpec {

  private val nino       = Nino("AA123456B")
  private val businessId = BusinessId("XAIS12345678901")
  private val taxYear    = TaxYear.fromMtd("2021-22")

  implicit private val correlationId: String = "X-123"

  private val inputData: DeletePropertyAnnualSubmissionRawData =
    DeletePropertyAnnualSubmissionRawData(nino.nino, businessId.businessId, taxYear.asMtd)

  trait Test extends MockDeletePropertyAnnualSubmissionValidator {
    lazy val parser = new DeletePropertyAnnualSubmissionRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockDeletePropertyAnnualSubmissionValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(DeletePropertyAnnualSubmissionRequestData(nino, businessId, taxYear))
      }
    }
    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockDeletePropertyAnnualSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
      "multiple validation errors occur" in new Test {
        MockDeletePropertyAnnualSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }

}
