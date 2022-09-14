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

import fixtures.AmendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPiePeriodSummaryFixtures
import support.UnitSpec
import v2.mocks.validators.MockAmendHistoricNonFhlUkPiePeriodSummaryValidator
import v2.models.domain.{ Nino, PeriodId }
import v2.models.errors.{ BadRequestError, ErrorWrapper, NinoFormatError, RuleBothExpensesSuppliedError }
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary.{
  AmendHistoricNonFhlUkPiePeriodSummaryRawData,
  AmendHistoricNonFhlUkPiePeriodSummaryRequest
}

class AmendHistoricNonFhlUkPiePeriodSummaryRequestParserSpec extends UnitSpec with AmendHistoricNonFhlUkPiePeriodSummaryFixtures {

  private val nino                   = "AA123456A"
  private val periodId               = "2017-04-06_2017-07-04"
  implicit val correlationId: String = "X-123"

  val inputData: AmendHistoricNonFhlUkPiePeriodSummaryRawData =
    AmendHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId, mtdJsonRequestFull)

  val consolidatedInputData: AmendHistoricNonFhlUkPiePeriodSummaryRawData =
    AmendHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId, mtdJsonRequestConsolidated)

  val invalidInputData: AmendHistoricNonFhlUkPiePeriodSummaryRawData =
    AmendHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId, invalidMtdRequestBodyJson)

  trait Test extends MockAmendHistoricNonFhlUkPiePeriodSummaryValidator {
    lazy val parser = new AmendHistoricNonFhlUkPiePeriodSummaryRequestParser(mockValidator)
  }

  "The request parser" should {

    "return a request object" when {
      "valid unconsolidated request data is supplied" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryValidator.validate(inputData).returns(Nil)
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPiePeriodSummaryRequest] = parser.parseRequest(inputData)

        result shouldBe Right(AmendHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId), requestBodyFull))
      }

      "valid consolidated request data is supplied" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryValidator.validate(consolidatedInputData).returns(Nil)
        val result: Either[ErrorWrapper, AmendHistoricNonFhlUkPiePeriodSummaryRequest] = parser.parseRequest(consolidatedInputData)

        result shouldBe Right(AmendHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId), requestBodyConsolidated))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryValidator
          .validate(inputData)
          .returns(List(NinoFormatError, RuleBothExpensesSuppliedError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, RuleBothExpensesSuppliedError))))
      }
    }
  }
}
