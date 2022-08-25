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
import v2.models.domain.Nino
import v2.models.errors.{ ErrorWrapper, NinoFormatError }

class ListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  implicit val correlationId: String = "X-123"

  val rawData: ListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRawData = ListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRawData(nino)

  trait Test extends MockListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryValidator {
    lazy val parser = new ListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequestParser(mockValidator)
  }

  "ListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequestParser" should {
    "return a valid request object" when {
      "valid raw data is supplied" in new Test {
        MockListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe Right(ListHistoricFhlUkPropertyIncomeExpensesPeriodSummaryRequest(Nino(nino)))
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
