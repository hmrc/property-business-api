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

import play.api.libs.json.Json
import support.UnitSpec
import v2.mocks.validators.MockCreateForeignPropertyPeriodSummaryValidator
import v2.models.domain.{Nino, TaxYear}
import v2.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import v2.models.request.common.foreignFhlEea.{CreateForeignFhlEea, ForeignFhlEeaIncome}
import v2.models.request.createForeignPropertyPeriodSummary._

class CreateForeignPropertyPeriodSummaryRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val businessId: String             = "XAIS12345678901"
  val taxYear: String                = "2022-23"
  implicit val correlationId: String = "X-123"

  // Simple case as JSON reads are tested elsewhere...
  private val requestBodyJson = Json.parse(
    """
      |{
      |   "fromDate":"2021-01-01",
      |   "toDate":"2021-03-29",
      |   "foreignFhlEea":{
      |      "income":{
      |         "rentAmount":381.21
      |      }
      |   }
      |}
      |""".stripMargin
  )

  val inputData: CreateForeignPropertyPeriodSummaryRawData =
    CreateForeignPropertyPeriodSummaryRawData(nino = nino, businessId = businessId, taxYear = taxYear, body = requestBodyJson)

  trait Test extends MockCreateForeignPropertyPeriodSummaryValidator {
    lazy val parser = new CreateForeignPropertyPeriodSummaryRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateForeignPropertyValidator.validate(inputData).returns(Nil)

        val model: CreateForeignPropertyPeriodSummaryRequestBody = CreateForeignPropertyPeriodSummaryRequestBody(
          fromDate = "2021-01-01",
          toDate = "2021-03-29",
          foreignFhlEea = Some(
            CreateForeignFhlEea(
              income = Some(ForeignFhlEeaIncome(Some(381.21))),
              expenses = None
            )),
          foreignNonFhlProperty = None
        )

        parser.parseRequest(inputData) shouldBe
          Right(CreateForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), model))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockCreateForeignPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockCreateForeignPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
