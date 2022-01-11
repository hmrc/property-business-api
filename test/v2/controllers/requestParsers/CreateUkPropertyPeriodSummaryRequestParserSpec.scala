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
import v2.mocks.validators.MockCreateUkPropertyPeriodSummaryValidator
import v2.models.domain.Nino
import v2.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import v2.models.request.common.ukFhlProperty.{UkFhlProperty, UkFhlPropertyIncome}
import v2.models.request.createUkPropertyPeriodSummary._

class CreateUkPropertyPeriodSummaryRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2022-23"
  val businessId: String             = "XAIS12345678901"
  implicit val correlationId: String = "X-123"

  // Simple case as JSON reads are tested elsewhere...
  private val requestBodyJson = Json.parse(
    """{
      |    "fromDate": "2020-01-01",
      |    "toDate": "2020-01-31",
      |    "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99
      |        }
      |    }
      |}
      |""".stripMargin
  )

  val inputData: CreateUkPropertyPeriodSummaryRawData =
    CreateUkPropertyPeriodSummaryRawData(nino, taxYear, businessId, requestBodyJson)

  trait Test extends MockCreateUkPropertyPeriodSummaryValidator {
    lazy val parser = new CreateUkPropertyPeriodSummaryRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateUkPropertyValidator.validate(inputData).returns(Nil)

        val model: CreateUkPropertyPeriodSummaryRequestBody = CreateUkPropertyPeriodSummaryRequestBody(
          fromDate = "2020-01-01",
          toDate = "2020-01-31",
          ukFhlProperty = Some(
            UkFhlProperty(
              income = Some(UkFhlPropertyIncome(periodAmount = Some(5000.99), None, None)),
              expenses = None
            )),
          ukNonFhlProperty = None
        )

        parser.parseRequest(inputData) shouldBe
          Right(CreateUkPropertyPeriodSummaryRequest(Nino(nino), taxYear, businessId, model))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockCreateUkPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockCreateUkPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
