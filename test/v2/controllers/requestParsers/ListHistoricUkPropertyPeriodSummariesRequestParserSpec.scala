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
import v2.mocks.validators.MockListHistoricUkPropertyPeriodSummariesValidator
import api.models.domain.Nino
import api.models.errors.{ ErrorWrapper, NinoFormatError }
import v2.models.request.listHistoricUkPropertyPeriodSummaries.{
  ListHistoricUkPropertyPeriodSummariesRawData,
  ListHistoricUkPropertyPeriodSummariesRequest
}

class ListHistoricUkPropertyPeriodSummariesRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  implicit val correlationId: String = "X-123"

  val rawData: ListHistoricUkPropertyPeriodSummariesRawData = ListHistoricUkPropertyPeriodSummariesRawData(nino)

  trait Test extends MockListHistoricUkPropertyPeriodSummariesValidator {
    lazy val parser = new ListHistoricUkPropertyPeriodSummariesRequestParser(mockValidator)
  }

  "ListHistoricUkPropertyPeriodSummariesRequestParser" should {
    "return a valid request object" when {
      "valid raw data is supplied" in new Test {
        MockListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe Right(ListHistoricUkPropertyPeriodSummariesRequest(Nino(nino)))
      }
    }

    "return an ErrorWrapper object" when {
      "the raw data contains single validation error" in new Test {

        MockListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryValidator
          .validate(rawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

    }
  }
}
