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
import v2.models.errors.{ BadRequestError, BusinessIdFormatError, ErrorWrapper, NinoFormatError }
import v2.models.request.common.ukFhlProperty.{ UkFhlProperty, UkFhlPropertyIncome }
import v2.models.request.createUkPropertyPeriodSummary._

class CreateHistoricNonFhlUkPropertyPeriodSubmissionRequestParserSpec extends UnitSpec {

  val nino: String                   = "AA123456B"
  val historicTaxYear: String        = "2022-23"
  val businessId: String             = "XAIS12345678901"
  implicit val correlationId: String = "X-123"

  private val requestBodyJson = Json.parse(
    """{
      | "fromDate": "2019-03-11",
      | "toDate": "2020-04-23", 
      |   "income": {
      |   "periodAmount": 123.45,
      |   "premiumsOfLeaseGrant": 2355.45,
      |   "reversePremiums": 454.56,
      |   "otherIncome": 567.89,
      |   "taxDeducted": 234.53,  
      |   "rentARoom": {
      |      "rentsReceived": 567.56
      |    }
      |   },
      |  "expenses":{
      |    "premisesRunningCosts": 567.53,
      |    "repairsAndMaintenance": 324.65,
      |    "financialCosts": 453.56,
      |    "professionalFees": 535.78,
      |    "costOfServices": 678.34,
      |    "other": 682.34, 
      |    "travelCosts": 645.56,
      |    "residentialFinancialCostsCarriedForward": 672.34,
      |    "residentialFinancialCost": 1000.45,
      |    "rentARoom": {
      |      "amountClaimed": 545.9
      |    }
      |  }
      |}
      |""".stripMargin
  )

  val inputData: CreateHistoricNonFhlUkPropertyPeriodSubmissionRawData =
    CreateHistoricNonFhlUkPropertyPeriodSubmissionRawData(nino, historicTaxYear, requestBodyJson)

  trait Test extends MockCreateHistoricNonFhlUkPropertyPeriodSubmissionValidator {
    lazy val parser = new CreateHistoricNonFhlUkPropertyPeriodSubmissionRequestParser(mockValidator)
  }

  "parse" should {

    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockCreateHistoricNonFhlUkPropertyPeriodSubmissionValidator.validate(inputData).returns(Nil)

        val model: CreateHistoricNonFhlUkPropertyPeriodSubmissionRequestBody = CreateHistoricNonFhlUkPropertyPeriodSubmissionRequestBody(
          fromDate = "2019-03-11",
          toDate = "2020-04-23",
          ukFhlProperty = Some(
            UkFhlProperty(
              income = Some(UkFhlPropertyIncome(periodAmount = Some(5000.99), None, None)),
              expenses = None
            )),
          ukNonFhlProperty = None
        )

        parser.parseRequest(inputData) shouldBe
          Right(CreateHistoricNonFhlUkPropertyPeriodSubmissionRequest(Nino(nino), taxYear, businessId, model))
      }
    }

    "return an ErrorWrapper" when {

      "a single validation error occurs" in new Test {
        MockCreateHistoricNonFhlUkPropertyPeriodSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockCreateHistoricNonFhlUkPropertyPeriodSubmissionValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError))))
      }
    }
  }
}
