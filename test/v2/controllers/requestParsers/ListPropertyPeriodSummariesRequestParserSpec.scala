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

import api.mocks.validators.MockListPropertyPeriodSummariesValidator
import support.UnitSpec
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError }
import v2.models.request.listPropertyPeriodSummaries._

class ListPropertyPeriodSummariesRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val businessId: String             = "XAIS12345678901"
  val taxYear: String                = "2021-22"
  implicit val correlationId: String = "X-123"

  val rawData: ListPropertyPeriodSummariesRawData = ListPropertyPeriodSummariesRawData(nino, businessId, taxYear)

  trait Test extends MockListPropertyPeriodSummariesValidator {
    lazy val parser = new ListPropertyPeriodSummariesRequestParser(mockValidator)
  }

  "ListPropertyPeriodSummariesRequestParser" should {
    "return a valid request object" when {
      "valid raw data is supplied" in new Test {
        MockListPropertyPeriodSummariesValidator.validate(rawData).returns(Nil)

        parser.parseRequest(rawData) shouldBe Right(ListPropertyPeriodSummariesRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear)))
      }
    }

    "return an ErrorWrapper object" when {
      "the raw data contains single validation error" in new Test {

        MockListPropertyPeriodSummariesValidator
          .validate(rawData)
          .returns(List(NinoFormatError))

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "the raw data contains multiple validation errors" in new Test {
        MockListPropertyPeriodSummariesValidator
          .validate(rawData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(rawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
