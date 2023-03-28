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

import api.models.domain.{Nino, PeriodId}
import support.UnitSpec
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, PeriodIdFormatError}
import v2.mocks.validators.MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator
import v2.models.request.retrieveHistoricNonFhlUkPiePeriodSummary.{RetrieveHistoricNonFhlUkPiePeriodSummaryRawData, RetrieveHistoricNonFhlUkPiePeriodSummaryRequest}

class RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val periodId: String               = "2017-04-06_2017-07-04"
  implicit val correlationId: String = "X-12345"

  val inputData: RetrieveHistoricNonFhlUkPiePeriodSummaryRawData =
    RetrieveHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId)

  trait Test extends MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator {
    lazy val parser = new RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(RetrieveHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId)))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "multiple validation errors occur" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError, PeriodIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, PeriodIdFormatError))))
      }
    }
  }
}
