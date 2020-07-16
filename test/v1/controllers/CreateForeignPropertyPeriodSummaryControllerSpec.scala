/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockCreateForeignPropertyPeriodSummaryRequestParser
import v1.mocks.services.{MockAuditService, MockCreateForeignPropertyPeriodSummaryService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.Method.GET
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.common.foreignFhlEea.{ForeignFhlEea, ForeignFhlEeaExpenditure, ForeignFhlEeaIncome}
import v1.models.request.common.foreignPropertyEntry.{ForeignPropertyEntry, ForeignPropertyExpenditure, ForeignPropertyIncome, ForeignPropertyRentIncome}
import v1.models.request.createForeignPropertyPeriodSummary._
import v1.models.response.createForeignPropertyPeriodSummary.{CreateForeignPropertyHateoasData, CreateForeignPropertyResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryControllerSpec
  extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockCreateForeignPropertyPeriodSummaryService
  with MockCreateForeignPropertyPeriodSummaryRequestParser
  with MockHateoasFactory
  with MockAuditService{

  trait Test {
    val hc = HeaderCarrier()

    val controller = new CreateForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateForeignPropertyService,
      parser = mockCreateForeignPropertyRequestParser,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val nino = "AA123456A"
  private val businessId = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val testHateoasLink = Link(href = "/individuals/business/property/TC663795B/XAIS12345678910/period", method = GET, rel="self")

  private val consolidatedRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99,
      |      "taxDeducted": 5000.99
      |    },
      |    "expenditure": {
      |      "consolidatedExpenses": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99,
      |          "taxDeducted": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "consolidatedExpenses": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val unconsolidatedRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99,
      |      "taxDeducted": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99,
      |          "taxDeducted": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costsOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val responseBody = Json.parse(
    """
      |{
      |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |  "links": [
      |    {
      |      "href":"/individuals/business/property/TC663795B/XAIS12345678910/period",
      |      "method":"GET",
      |      "rel":"self"
      |    }
      |  ]
      |}
      |""".stripMargin)

  val response = CreateForeignPropertyResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")


  private val foreignFhlEea = ForeignFhlEea(ForeignFhlEeaIncome(2000.99, Some(2000.99)),
    Some(ForeignFhlEeaExpenditure(
      Some(2000.99),
      Some(2000.99),
      Some(2000.99),
      Some(2000.99),
      Some(2000.99),
      Some(2000.99),
      Some(2000.99),
      Some(2000.99)
    )))

  private val foreignProperty = ForeignPropertyEntry("FRA", ForeignPropertyIncome(
    ForeignPropertyRentIncome(2000.99, 2000.99), true, Some(2000.99), Some(2000.99), Some(2000.99), Some(2000.99)),
  Some(ForeignPropertyExpenditure(Some(2000.99),
    Some(2000.99),
    Some(2000.99),
    Some(2000.99),
    Some(2000.99),
    Some(2000.99),
    Some(2000.99),
    Some(2000.99),
    Some(2000.99),
    Some(2000.99)
  )))


  private val requestBody = CreateForeignPropertyRequestBody("2019-01-01", "2018-01-01", Some(foreignFhlEea), Some(Seq(foreignProperty)))
  private val requestData = CreateForeignPropertyRequestData(Nino(nino), businessId, requestBody)
  private val rawData = CreateForeignPropertyRawData(nino, businessId, consolidatedRequestBodyJson)

  "create" should {
    "return a successful response from a consolidated request" when {
      "the request received is valid" in new Test {

        MockCreateForeignPropertyRequestParser
          .requestFor(CreateForeignPropertyRawData(nino, businessId, consolidatedRequestBodyJson))
          .returns(Right(requestData))

        MockCreateForeignPropertyService
          .createForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateForeignPropertyHateoasData(nino, businessId, submissionId))
          .returns(HateoasWrapper(response, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId)(fakePostRequest(consolidatedRequestBodyJson))
        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe responseBody
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return a successful response from an unconsolidated request" when {
      "the request received is valid" in new Test {

        MockCreateForeignPropertyRequestParser
          .requestFor(CreateForeignPropertyRawData(nino, businessId, unconsolidatedRequestBodyJson))
          .returns(Right(requestData))

        MockCreateForeignPropertyService
          .createForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateForeignPropertyHateoasData(nino, businessId, submissionId))
          .returns(HateoasWrapper(response, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId)(fakePostRequest(unconsolidatedRequestBodyJson))
        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe responseBody
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateForeignPropertyRequestParser
              .requestFor(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId)(fakePostRequest(consolidatedRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (CountryCodeFormatError, BAD_REQUEST),
          (RuleBothExpensesSuppliedError, BAD_REQUEST),
          (RuleToDateBeforeFromDateError, BAD_REQUEST),
          (RuleCountryCodeError, BAD_REQUEST),
          (RuleOverlappingPeriodError, BAD_REQUEST),
          (RuleMisalignedPeriodError, BAD_REQUEST),
          (RuleNotContiguousPeriodError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateForeignPropertyRequestParser
              .requestFor(rawData)
              .returns(Right(requestData))

            MockCreateForeignPropertyService
              .createForeignProperty(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId)(fakePostRequest(consolidatedRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
