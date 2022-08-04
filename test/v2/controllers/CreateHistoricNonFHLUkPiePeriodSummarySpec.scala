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

package v2.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockCreateUkPropertyPeriodSummaryRequestParser
import v2.mocks.services.{MockAuditService, MockCreateUkPropertyPeriodSummaryService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.{CreateHistoricNonFhlUkPropertyPeriodSummaryRawData, CreateHistoricNonFhlUkPropertyPeriodSummaryRequest, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody, UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}
import v2.models.request.createUkPropertyPeriodSummary._
import v2.models.response.createUkPropertyPeriodSummary._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateHistoricNonFHLUkPiePeriodSummarySpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateHistoricNonFhlUkPiePeriodSummaryService
    with MockCreateHistoricNonFHLUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val periodId      = "2019-03-11_2020-04-23"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateHistoricNonFHLUkPiePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateNonFHLUKPiePeriodSummaryService,
      auditService = mockAuditService,
      parser = mockCreateUkNonFHLUkPiePeriodSummaryRequestParser,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  val requestBody: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
      fromDate = "2020-01-01",
      toDate = "2020-01-31",
      income = Some(
        UkNonFhlPropertyIncome(
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(UkPropertyIncomeRentARoom(Some(5000.99)))
        )
      ),
      expenses = Some(
        UkNonFhlPropertyExpenses(
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(UkPropertyExpensesRentARoom(Some(5000.99))),
          Some(0)
        )
      )
    )


  val requestBodyWithConsolidatedExpense: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(
      fromDate= "2020-01-01",
      toDate = "2020-01-31",
      income = Some(
        UkNonFhlPropertyIncome(
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(UkPropertyIncomeRentARoom(Some(5000.99)))
        )
      ),
      expenses = Some(
        UkNonFhlPropertyExpenses(
          Some(0),
          Some(0),
          Some(0),
          Some(0),
          Some(0),
          Some(0),
          Some(0),
          Some(0),
          Some(0),
          Some(UkPropertyExpensesRentARoom(Some(0))),
          Some(5000.99)
        )
      )
    )

  private val requestBodyJson = Json.parse(
    """
      |{
      | "fromDate": "2019-03-11",
      | "todate": "2020-04-23",
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

  private val requestBodyJsonConsolidatedExpense = Json.parse(
    """
      |{
      |    "fromDate": "2019-03-11",
      |    "toDate": "2020-04-23",
      |    "income": {
      |        "periodAmount": 123.45,
      |        "premiumsOfLeaseGrant": 2355.45,
      |        "reversePremiums": 454.56,
      |        "otherIncome": 567.89,
      |        "taxDeducted": 234.53,
      |        "rentARoom": {
      |           "rentsReceived": 567.56
      |         }
      |        },
      |       "expenses":{
      |          "consolidatedExpenses": 235.78
      |     }
      |}
      |""".stripMargin
  )

  private val rawData     = CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(nino, requestBodyJson)
  private val requestData = CreateHistoricNonFhlUkPropertyPeriodSummaryRequest(Nino(nino), rawData)


  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |  "periodId": "$periodId",
       |  "links": [
       |    {
       |      "href": "/individuals/business/property/uk/non-furnished-holiday-lettings/$nino/$periodId",
       |      "method": "PUT",
       |      "rel": "amend-uk-property-historic-non-fhl-period-summary"
       |    },
       |    {
       |      "href": /individuals/business/property/uk/non-furnished-holiday-lettings/$nino/$periodId",
       |      "method": "GET",
       |      "rel": "self"
       |    }
       |  ]
       |}
       |""".stripMargin
  )

  def event(requestBody: JsValue, auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateHistoricNonFhlUkPropertyPeriodSummary",
      transactionName = "create-uk-property-historic-non-fhl-period-summary",
      detail = GenericAuditDetail(
        versionNumber = "2.0",
        userType = "Individual",
        agentReferenceNumber = None,
        params = Json.obj("nino" -> nino, "request" -> requestBody),
        correlationId = correlationId,
        response = auditResponse
      )
    )

  //TODO: Make a response model here
  val response: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse(
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  private val testHateoasLink =
    Link(href = s"/individuals/business/property/uk/non-furnished-holiday-lettings/$nino/$periodId",
      method = GET,
      rel = "self")

  "create" should {
    "return a successful response from a consolidated request" when {
      "the request received is valid" in new Test {

        MockCreateUkPropertyRequestParser
          .requestFor(CreateUkPropertyPeriodSummaryRawData(nino, taxYear, businessId, requestBodyJsonConsolidatedExpense))
          .returns(Right(requestData))

        MockCreateUkPropertyService
          .createUkProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))
          .returns(HateoasWrapper(response, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestBodyJsonConsolidatedExpense))
        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(CREATED, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(requestBodyJsonConsolidatedExpense, auditResponse)).once
      }
    }

    "return a successful response from an unconsolidated request" when {
      "the request received is valid" in new Test {

        MockCreateUkPropertyRequestParser
          .requestFor(CreateUkPropertyPeriodSummaryRawData(nino, taxYear, businessId, requestBodyJson))
          .returns(Right(requestData))

        MockCreateUkPropertyService
          .createUkProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, CreateUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))
          .returns(HateoasWrapper(response, Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestBodyJson))
        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe hateoasResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(CREATED, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(requestBodyJson, auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateUkPropertyRequestParser
              .requestFor(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(requestBodyJson, auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (RuleBothExpensesSuppliedError, BAD_REQUEST),
          (RuleToDateBeforeFromDateError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (InternalError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateUkPropertyRequestParser
              .requestFor(rawData)
              .returns(Right(requestData))

            MockCreateUkPropertyService
              .createUkProperty(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequestWithBody(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(requestBodyJson, auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTypeOfBusinessIncorrectError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (InternalError, INTERNAL_SERVER_ERROR),
          (RuleOverlappingPeriodError, BAD_REQUEST),
          (RuleMisalignedPeriodError, BAD_REQUEST),
          (RuleNotContiguousPeriodError, BAD_REQUEST),
          (RuleToDateBeforeFromDateError, BAD_REQUEST),
          (RuleDuplicateSubmissionError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
