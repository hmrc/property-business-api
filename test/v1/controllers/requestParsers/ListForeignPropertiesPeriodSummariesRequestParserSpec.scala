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
import v1.mocks.support.MockDateUtils
import v1.mocks.validators.MockListForeignPropertiesPeriodSummariesValidator
import v1.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import v1.models.request.listForeignPropertiesPeriodSummaries.{ListForeignPropertiesPeriodSummariesRawData, ListForeignPropertiesPeriodSummariesRequest}

class ListForeignPropertiesPeriodSummariesRequestParserSpec extends UnitSpec {
  val nino = "AA123456B"
  val businessId = "XAIS12345678901"
  val fromDate = "2020-06-06"
  val toDate = "2020-08-06"
  implicit val correlationId = "X-123"

  val inputData =
    ListForeignPropertiesPeriodSummariesRawData(nino, businessId, Some(fromDate), Some(toDate))
  val inputDataWithoutDates =
    ListForeignPropertiesPeriodSummariesRawData(nino, businessId, None, None)

  trait Test extends MockListForeignPropertiesPeriodSummariesValidator with MockDateUtils {
    lazy val parser = new ListForeignPropertiesPeriodSummariesRequestParser(mockValidator, mockDateUtils)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockListForeignPropertiesValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(ListForeignPropertiesPeriodSummariesRequest(Nino(nino), businessId, fromDate, toDate))
      }
      "valid request data is supplied without dates" in new Test {
        MockListForeignPropertiesValidator.validate(inputDataWithoutDates).returns(Nil)
        MockDateUtils.currentTaxYearStart().returns("start")
        MockDateUtils.currentTaxYearEnd().returns("end")

        parser.parseRequest(inputDataWithoutDates) shouldBe Right(ListForeignPropertiesPeriodSummariesRequest(Nino(nino), businessId, "start", "end"))
      }
    }
    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockListForeignPropertiesValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }
      "multiple validation errors occur" in new Test {
        MockListForeignPropertiesValidator.validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
