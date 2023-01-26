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

import support.UnitSpec
import v2.mocks.validators.MockDeleteHistoricUkPropertyAnnualSubmissionValidator
import v2.models.domain.{ HistoricPropertyType, Nino, TaxYear }
import v2.models.errors._
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.{
  DeleteHistoricUkPropertyAnnualSubmissionRawData,
  DeleteHistoricUkPropertyAnnualSubmissionRequest
}

class DeleteHistoricUkPropertyAnnualSubmissionRequestParserSpec extends UnitSpec {

  val nino: String                       = "AA123456B"
  val taxYear: String                    = "2021-22"
  val propertyType: HistoricPropertyType = HistoricPropertyType.Fhl

  implicit val correlationId: String = "X-123"

  val inputData: DeleteHistoricUkPropertyAnnualSubmissionRawData =
    DeleteHistoricUkPropertyAnnualSubmissionRawData(nino, taxYear, propertyType)

  trait Test extends MockDeleteHistoricUkPropertyAnnualSubmissionValidator {
    lazy val parser = new DeleteHistoricUkPropertyAnnualSubmissionRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockDeleteHistoricUkPropertyAnnualSubmissionValidator.validate(inputData).returns(Nil)
        parser.parseRequest(inputData) shouldBe Right(
          DeleteHistoricUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear), propertyType))
      }
    }
    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockDeleteHistoricUkPropertyAnnualSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
      "multiple validation errors occur" in new Test {
        MockDeleteHistoricUkPropertyAnnualSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError, TaxYearFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }
}
