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
import v2.models.domain.{Nino, TaxYear}
import v2.models.errors.{BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError}
import v2.models.request.amendUkPropertyPeriodSummary._
import v2.models.request.common.ukFhlProperty._
import v2.mocks.validators.MockAmendUkPropertyPeriodSummaryValidator

class AmendUkPropertyPeriodSummaryRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2022-23"
  val businessId: String             = "XAIS12345678901"
  val submissionId: String           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "X-123"

  private val requestBodyJson = Json.parse(
    """{
      |    "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99
      |        }
      |    }
      |}
      |""".stripMargin
  )

  val inputData: AmendUkPropertyPeriodSummaryRawData =
    AmendUkPropertyPeriodSummaryRawData(nino, taxYear, businessId, submissionId, requestBodyJson)

  trait Test extends MockAmendUkPropertyPeriodSummaryValidator {
    lazy val parser = new AmendUkPropertyPeriodSummaryRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendUkPropertyValidator.validate(inputData).returns(Nil)

        val model: AmendUkPropertyPeriodSummaryRequestBody = AmendUkPropertyPeriodSummaryRequestBody(
          ukFhlProperty = Some(
            UkFhlProperty(
              income = Some(UkFhlPropertyIncome(periodAmount = Some(5000.99), None, None)),
              expenses = None
            )),
          ukNonFhlProperty = None
        )

        parser.parseRequest(inputData) shouldBe
          Right(AmendUkPropertyPeriodSummaryRequest(Nino(nino), TaxYear.fromMtd(taxYear), businessId, submissionId, model))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendUkPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendUkPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
