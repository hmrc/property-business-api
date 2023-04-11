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

import play.api.libs.json.Json
import support.UnitSpec
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import v2.mocks.validators.MockAmendForeignPropertyPeriodSummaryValidator
import v2.models.request.amendForeignPropertyPeriodSummary._
import v2.models.request.common.foreignFhlEea._

class AmendForeignPropertyPeriodSummaryRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val taxYear: String                = "2022-23"
  val businessId: String             = "XAIS12345678901"
  val submissionId: String           = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "X-123"

  private val requestBodyJson = Json.parse(
    """{
      |    "foreignFhlEea":{
      |        "income": {
      |            "rentAmount": 5000.99
      |        }
      |    }
      |}
      |""".stripMargin
  )

  val inputData: AmendForeignPropertyPeriodSummaryRawData =
    AmendForeignPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId, requestBodyJson)

  trait Test extends MockAmendForeignPropertyPeriodSummaryValidator {
    lazy val parser = new AmendForeignPropertyPeriodSummaryRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockAmendForeignPropertyValidator.validate(inputData).returns(Nil)

        val model: AmendForeignPropertyPeriodSummaryRequestBody = AmendForeignPropertyPeriodSummaryRequestBody(
          foreignFhlEea = Some(
            AmendForeignFhlEea(
              income = Some(ForeignFhlEeaIncome(Some(5000.99))),
              expenses = None
            )),
          foreignNonFhlProperty = None
        )

        parser.parseRequest(inputData) shouldBe
          Right(AmendForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), submissionId, model))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockAmendForeignPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockAmendForeignPropertyValidator
          .validate(inputData)
          .returns(List(NinoFormatError, BusinessIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, BusinessIdFormatError))))
      }
    }
  }
}
