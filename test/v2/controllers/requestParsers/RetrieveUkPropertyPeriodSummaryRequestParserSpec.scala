/*
 * Copyright 2022 HM Revenue & Customs
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
import v2.mocks.validators.MockRetrieveUkPropertyPeriodSummaryValidator
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors._
import v2.models.request.retrieveUkPropertyPeriodSummary.{ RetrieveUkPropertyPeriodSummaryRawData, RetrieveUkPropertyPeriodSummaryRequest }

class RetrieveUkPropertyPeriodSummaryRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val businessId: String             = "XAIS12345678901"
  val taxYear: String                = "2022-23"
  val submissionId: String           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val inputData: RetrieveUkPropertyPeriodSummaryRawData = RetrieveUkPropertyPeriodSummaryRawData(
    nino = nino,
    businessId = businessId,
    taxYear = taxYear,
    submissionId = submissionId
  )

  trait Test extends MockRetrieveUkPropertyPeriodSummaryValidator {
    lazy val parser = new RetrieveUkPropertyPeriodSummaryRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveUkPropertyPeriodSummaryValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(
          RetrieveUkPropertyPeriodSummaryRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), submissionId))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveUkPropertyPeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockRetrieveUkPropertyPeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError, TaxYearFormatError, SubmissionIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(
            ErrorWrapper(correlationId,
                         BadRequestError,
                         Some(Seq(NinoFormatError, BusinessIdFormatError, TaxYearFormatError, SubmissionIdFormatError))))
      }
    }
  }
}
