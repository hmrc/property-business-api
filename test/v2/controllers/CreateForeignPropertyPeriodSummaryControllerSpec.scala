/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockCreateForeignPropertyPeriodSummaryRequestParser
import v2.mocks.services.{MockAuditService, MockCreateForeignPropertyPeriodSummaryService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.audit.{AuditError, AuditEvent, AuditResponse, CreateForeignPropertyPeriodicAuditDetail}
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._
import v2.models.request.createForeignPropertyPeriodSummary._
import v2.models.response.createForeignPropertyPeriodSummary._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateForeignPropertyPeriodSummaryService
    with MockCreateForeignPropertyPeriodSummaryRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino = "AA123456A"
  private val businessId = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateForeignPropertyService,
      auditService = mockAuditService,
      parser = mockCreateForeignPropertyRequestParser,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val testHateoasLink = Link(href = "/individuals/business/property/TC663795B/XAIS12345678910/period", method = GET, rel="self")

  private val consolidatedRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
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
    """.stripMargin
  )

  private val unconsolidatedRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
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
      |          "rentAmount": 5000.99
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
    """.stripMargin
  )

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
    """.stripMargin
  )

  val response: CreateForeignPropertyPeriodSummaryResponse = CreateForeignPropertyPeriodSummaryResponse(
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  private val foreignFhlEea = ForeignFhlEea(ForeignFhlEeaIncome(2000.99),
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
    ForeignPropertyRentIncome(2000.99), true, Some(2000.99), Some(2000.99), Some(2000.99), Some(2000.99)),
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

  private val requestBody = CreateForeignPropertyPeriodSummaryRequestBody("2019-01-01", "2018-01-01", Some(foreignFhlEea), Some(Seq(foreignProperty)))
  private val requestData = CreateForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, requestBody)
  private val rawData = CreateForeignPropertyPeriodSummaryRawData(nino, businessId, consolidatedRequestBodyJson)

  val hateoasResponse: JsValue = Json.parse(
    """
      |{
      |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
      |        "links": [
      |          {
      |            "href": "/individuals/business/property/TC663795B/XAIS12345678910/period",
      |            "method": "GET",
      |            "rel": "self"
      |          }
      |        ]
      |      }
    """.stripMargin
  )

  def consolidatedEvent(auditResponse: AuditResponse): AuditEvent[CreateForeignPropertyPeriodicAuditDetail] =
    AuditEvent(
      auditType = "CreateForeignPropertyIncomeAndExpenditurePeriodSummary",
      transactionName = "Create-Foreign-Property-Income-And-Expenditure-Period-Summary",
      detail = CreateForeignPropertyPeriodicAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        businessId,
        consolidatedRequestBodyJson,
        correlationId,
        response = auditResponse
      )
    )

  def unconsolidatedEvent(auditResponse: AuditResponse): AuditEvent[CreateForeignPropertyPeriodicAuditDetail] =
    AuditEvent(
      auditType = "CreateForeignPropertyIncomeAndExpenditurePeriodSummary",
      transactionName = "Create-Foreign-Property-Income-And-Expenditure-Period-Summary",
      detail = CreateForeignPropertyPeriodicAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        businessId,
        unconsolidatedRequestBodyJson,
        correlationId,
        response = auditResponse
      )
    )

  "create" should {
    "return a successful response from a consolidated request" when {
      "the request received is valid" in new Test {

        MockCreateForeignPropertyRequestParser
          .requestFor(CreateForeignPropertyPeriodSummaryRawData(nino, businessId, consolidatedRequestBodyJson))
          .returns(Right(requestData))

        MockCreateForeignPropertyService
          .createForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateForeignPropertyPeriodSummaryHateoasData(nino, businessId, submissionId))
          .returns(HateoasWrapper(response, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId)(fakePostRequest(consolidatedRequestBodyJson))
        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe responseBody
        header("X-CorrelationId", result) shouldBe Some(correlationId)


        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(consolidatedEvent(auditResponse)).once
      }
    }

    "return a successful response from an unconsolidated request" when {
      "the request received is valid" in new Test {

        MockCreateForeignPropertyRequestParser
          .requestFor(CreateForeignPropertyPeriodSummaryRawData(nino, businessId, unconsolidatedRequestBodyJson))
          .returns(Right(requestData))

        MockCreateForeignPropertyService
          .createForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateForeignPropertyPeriodSummaryHateoasData(nino, businessId, submissionId))
          .returns(HateoasWrapper(response, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId)(fakePostRequest(unconsolidatedRequestBodyJson))
        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe responseBody
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(unconsolidatedEvent(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateForeignPropertyRequestParser
              .requestFor(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId)(fakePostRequest(consolidatedRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(consolidatedEvent(auditResponse)).once
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
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId)(fakePostRequest(consolidatedRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)


            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(consolidatedEvent(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (RuleOverlappingPeriodError, BAD_REQUEST),
          (RuleMisalignedPeriodError, BAD_REQUEST),
          (RuleNotContiguousPeriodError, BAD_REQUEST),
          (RuleToDateBeforeFromDateError, BAD_REQUEST),
          (RuleDuplicateSubmission, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
