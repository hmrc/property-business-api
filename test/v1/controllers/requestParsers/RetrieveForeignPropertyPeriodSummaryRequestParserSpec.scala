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
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockRetrieveForeignPropertyPeriodSummaryValidator
import v1.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import v1.models.request.retrieveForeignPropertyPeriodSummary.{RetrieveForeignPropertyPeriodSummaryRawData, RetrieveForeignPropertyPeriodSummaryRequest}

class RetrieveForeignPropertyPeriodSummaryRequestParserSpec extends UnitSpec {
  val nino = "AA123456B"
  val businessId = "XAIS12345678901"
  val submissionId = "12345678-1234-4123-9123-123456789012"
  implicit val correlationId = "X-123"

  val inputData =
    RetrieveForeignPropertyPeriodSummaryRawData(nino, businessId, submissionId)

  trait Test extends MockRetrieveForeignPropertyPeriodSummaryValidator {
    lazy val parser = new RetrieveForeignPropertyPeriodSummaryRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveForeignPropertyValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(RetrieveForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, submissionId))
      }
    }
    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveForeignPropertyValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
      "multiple validation errors occur" in new Test {
        MockRetrieveForeignPropertyValidator.validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
