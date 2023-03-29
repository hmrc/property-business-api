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

package v2.controllers

import api.controllers.ControllerBaseSpec
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.mocks.MockIdGenerator
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.requestParsers.MockCreateUkPropertyPeriodSummaryRequestParser
import v2.mocks.services.MockCreateUkPropertyPeriodSummaryService
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import v2.models.request.common.ukFhlProperty.{UkFhlProperty, UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v2.models.request.common.ukNonFhlProperty.{UkNonFhlProperty, UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createUkPropertyPeriodSummary._
import v2.models.response.createUkPropertyPeriodSummary._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateUkPropertyPeriodSummaryService
    with MockCreateUkPropertyPeriodSummaryRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val taxYear       = "2020-21"
  private val businessId    = "XAIS12345678910"
  private val submissionId  = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateUkPropertyService,
      auditService = mockAuditService,
      parser = mockCreateUkPropertyRequestParser,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  val requestBody: CreateUkPropertyPeriodSummaryRequestBody =
    CreateUkPropertyPeriodSummaryRequestBody(
      "2020-01-01",
      "2020-01-31",
      Some(
        UkFhlProperty(
          Some(
            UkFhlPropertyIncome(
              Some(5000.99),
              Some(3123.21),
              Some(UkPropertyIncomeRentARoom(
                Some(532.12)
              ))
            )),
          Some(UkFhlPropertyExpenses(
            Some(3123.21),
            Some(928.42),
            Some(842.99),
            Some(8831.12),
            Some(484.12),
            Some(99282),
            Some(999.99),
            Some(974.47),
            Some(UkPropertyExpensesRentARoom(
              Some(8842.43)
            ))
          ))
        )),
      Some(
        UkNonFhlProperty(
          Some(
            UkNonFhlPropertyIncome(
              Some(41.12),
              Some(84.31),
              Some(9884.93),
              Some(842.99),
              Some(31.44),
              Some(UkPropertyIncomeRentARoom(
                Some(947.66)
              ))
            )),
          Some(
            UkNonFhlPropertyExpenses(
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              Some(988.18)
            ))
        ))
    )

  val requestBodyWithConsolidatedExpense: CreateUkPropertyPeriodSummaryRequestBody =
    CreateUkPropertyPeriodSummaryRequestBody(
      "2020-01-01",
      "2020-01-31",
      Some(
        UkFhlProperty(
          Some(
            UkFhlPropertyIncome(
              Some(5000.99),
              Some(3123.21),
              Some(UkPropertyIncomeRentARoom(
                Some(532.12)
              ))
            )),
          Some(
            UkFhlPropertyExpenses(
              None,
              None,
              None,
              None,
              None,
              None,
              Some(988.18),
              None,
              None
            ))
        )),
      Some(
        UkNonFhlProperty(
          Some(
            UkNonFhlPropertyIncome(
              Some(41.12),
              Some(84.31),
              Some(9884.93),
              Some(842.99),
              Some(31.44),
              Some(UkPropertyIncomeRentARoom(
                Some(947.66)
              ))
            )),
          Some(
            UkNonFhlPropertyExpenses(
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              Some(988.18)
            ))
        ))
    )

  private val requestBodyJson = Json.parse(
    """{
      |    "fromDate": "2020-01-01",
      |    "toDate": "2020-01-31",
      |    "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99,
      |            "taxDeducted": 3123.21,
      |            "rentARoom": {
      |                "rentsReceived": 532.12
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "travelCosts": 974.47,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            }
      |        }
      |    },
      |    "ukNonFhlProperty": {
      |        "income": {
      |            "premiumsOfLeaseGrant": 42.12,
      |            "reversePremiums": 84.31,
      |            "periodAmount": 9884.93,
      |            "taxDeducted": 842.99,
      |            "otherIncome": 31.44,
      |            "rentARoom": {
      |                "rentsReceived": 947.66
      |            }
      |        },
      |        "expenses": {
      |            "premisesRunningCosts": 3123.21,
      |            "repairsAndMaintenance": 928.42,
      |            "financialCosts": 842.99,
      |            "professionalFees": 8831.12,
      |            "costOfServices": 484.12,
      |            "other": 99282,
      |            "residentialFinancialCost": 12.34,
      |            "travelCosts": 974.47,
      |            "residentialFinancialCostsCarriedForward": 12.34,
      |            "rentARoom": {
      |                "amountClaimed": 8842.43
      |            }
      |        }
      |    }
      |}
      |""".stripMargin
  )

  private val requestBodyJsonConsolidatedExpense = Json.parse(
    """{
      |    "fromDate": "2020-01-01",
      |    "toDate": "2020-01-31",
      |    "ukFhlProperty":{
      |        "income": {
      |            "periodAmount": 5000.99,
      |            "taxDeducted": 3123.21,
      |            "rentARoom": {
      |                "rentsReceived": 532.12
      |            }
      |        },
      |        "expenses": {
      |            "consolidatedExpenses": 988.18
      |        }
      |    },
      |    "ukNonFhlProperty": {
      |        "income": {
      |            "premiumsOfLeaseGrant": 42.12,
      |            "reversePremiums": 84.31,
      |            "periodAmount": 9884.93,
      |            "taxDeducted": 842.99,
      |            "otherIncome": 31.44,
      |            "rentARoom": {
      |                "rentsReceived": 947.66
      |            }
      |        },
      |        "expenses": {
      |            "consolidatedExpenses": 988.18
      |        }
      |    }
      |}
      |""".stripMargin
  )

  private val requestData = CreateUkPropertyPeriodSummaryRequest(Nino(nino), TaxYear.fromMtd(taxYear), businessId, requestBody)
  private val rawData     = CreateUkPropertyPeriodSummaryRawData(nino, taxYear, businessId, requestBodyJson)

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
       |  "links": [
       |    {
       |      "href":"/individuals/business/property/uk/$nino/$businessId/period/$taxYear/4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
       |      "method":"GET",
       |      "rel":"self"
       |    }
       |  ]
       |}
    """.stripMargin
  )

  def event(requestBody: JsValue, auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "CreateUKPropertyIncomeAndExpensesPeriodSummary",
      transactionName = "create-uk-property-income-and-expenses-period-summary",
      detail = GenericAuditDetail(
        versionNumber = "2.0",
        userType = "Individual",
        agentReferenceNumber = None,
        params = Json.obj("nino" -> nino, "taxYear" -> taxYear, "businessId" -> businessId, "request" -> requestBody),
        correlationId = correlationId,
        response = auditResponse
      )
    )

  val response: CreateUkPropertyPeriodSummaryResponse = CreateUkPropertyPeriodSummaryResponse(
    submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  private val testHateoasLink =
    Link(
      href = s"/individuals/business/property/uk/$nino/$businessId/period/$taxYear/4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
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
          (RuleDuplicateSubmissionError, BAD_REQUEST),
          (RuleIncorrectGovTestScenarioError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
