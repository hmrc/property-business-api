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

import fixtures.CreateAmendNonFhlUkPropertyAnnualSubmission.RequestResponseModelFixtures
import support.UnitSpec
import v2.mocks.validators.MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors.{ BadRequestError, DateFormatError, ErrorWrapper, NinoFormatError }
import v2.models.request.createAmendHistoricNonFhlUkPropertyAnnualSubmission._

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParserSpec extends UnitSpec with RequestResponseModelFixtures {

  val nino: String                   = "AA123456B"
  val mtdTaxYear: String             = "2022-23"
  val taxYear: TaxYear               = TaxYear.fromMtd(mtdTaxYear)
  implicit val correlationId: String = "X-123"

  val inputData: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData =
    CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRawData(nino, mtdTaxYear, validMtdJson)

  trait Test extends MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator {
    lazy val parser = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestParser(mockValidator)
  }

  "The request parser" should {

    "return a parsed request object" when {
      "given valid request data" in new Test {
        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequest(Nino(nino), taxYear, requestBody))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError, DateFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, DateFormatError))))
      }
    }

  }

}
