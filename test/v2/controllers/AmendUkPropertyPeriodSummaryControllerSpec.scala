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
import v2.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.audit.{AuditError, AuditEvent, AuditResponse}
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendUkPropertyPeriodSummary.{AmendUkPropertyPeriodSummaryRawData, AmendUkPropertyPeriodSummaryRequest, AmendUkPropertyPeriodSummaryRequestBody}
import v2.models.request.common.ukFhlProperty.{UkFhlProperty, UkFhlPropertyExpenses, UkFhlPropertyIncome}
import v2.models.request.common.ukNonFhlProperty.{UkNonFhlProperty, UkNonFhlPropertyExpenses}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendUkPropertyPeriodSummaryControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendUkPropertyPeriodSummaryService
    with MockAmendUkPropertyPeriodSummaryRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val nino = "AA123456A"
  private val taxYear = "2020-21"
  private val businessId = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendUkPropertyRequestParser,
      service = mockService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  val requestBody: AmendUkPropertyPeriodSummaryRequestBody =
    AmendUkPropertyPeriodSummaryRequestBody(
      Some(UkFhlProperty(
        Some(UkFhlPropertyIncome(
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
      Some(UkNonFhlProperty(
        Some(UkNonFhlPropertyIncome(
          Some(41.12),
          Some(84.31),
          Some(9884.93),
          Some(842.99),
          Some(31.44),
          Some(UkPropertyIncomeRentARoom(
            Some(947.66)
          ))
        )),
        Some(UkNonFhlPropertyExpenses(
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

  val requestBodyWithConsolidatedExpense: AmendUkPropertyPeriodSummaryRequestBody =
    AmendUkPropertyPeriodSummaryRequestBody(
      Some(UkFhlProperty(
        Some(UkFhlPropertyIncome(
          Some(5000.99),
          Some(3123.21),
          Some(UkPropertyIncomeRentARoom(
            Some(532.12)
          ))
        )),
        Some(UkFhlPropertyExpenses(
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
      Some(UkNonFhlProperty(
        Some(UkNonFhlPropertyIncome(
          Some(41.12),
          Some(84.31),
          Some(9884.93),
          Some(842.99),
          Some(31.44),
          Some(UkPropertyIncomeRentARoom(
            Some(947.66)
          ))
        )),
        Some(UkNonFhlPropertyExpenses(
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

  private val requestData = AmendUkPropertyPeriodSummaryRequest(Nino(nino), taxYear, businessId, submissionId, requestBody)
  private val rawData = AmendUkPropertyPeriodSummaryRawData(nino, taxYear, businessId, submissionId, requestBodyJson)

  val hateoasResponse: JsValue = Json.parse(
    """
      |{
      |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
      |}
    """.stripMargin
  )

  def consolidatedEvent(auditResponse: AuditResponse): AuditEvent[AmendUkPropertyPeriodicAuditDetail] =
    AuditEvent(
      auditType = "AmendUkPropertyIncomeAndExpenditurePeriodSummary",
      transactionName = "Amend-Uk-Property-Income-And-Expenditure-Period-Summary",
      detail = AmendUkPropertyPeriodicAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino,
        taxYear,
        businessId,
        submissionId,
        requestBodyWithConsolidatedExpense,
        correlationId,
        response = auditResponse
      )
    )

  private val responseBody = Json.parse(
    """
      |{
      |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
      |}
    """.stripMargin
  )

  "amend" should {
    "return a successful response from a consolidated request" when {
      "the request received is valid" in new Test {

        MockAmendUkPropertyRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendUkPropertyService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendUkPropertyPeriodSummaryHateoasData(nino, taxYear, businessId, submissionId))
          .returns(HateoasWrapper(response, Seq.empty))

        val result: Future[Result] = controller.handleRequest(nino, taxYear, businessId, submissionId)(fakePostRequest(requestBodyJsonConsolidatedExpense))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(consolidatedEvent(auditResponse)).once
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendUkPropertyRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakePostRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (SubmissionIdFormatError, BAD_REQUEST),
          (CountryCodeFormatError.copy(paths = Some(Seq(
            "ukFhlProperty/0/countryCode"))), BAD_REQUEST),
          (ValueFormatError.copy(paths = Some(Seq(
            "ukFhlProperty/income/rentARoom",
            "ukFhlProperty/expenses/repairsAndMaintenance",
            "ukFhlProperty/expenses/professionalFees",
            "ukFhlProperty/expenses/other",
            "ukNonFhlProperty/income/rentARoom/amountClaimed",
            "ukNonFhlProperty/expenses/professionalFees",
            "ukNonFhlProperty/expenses/other"))), BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleBothExpensesSuppliedError, BAD_REQUEST),
          (RuleCountryCodeError.copy(paths = Some(Seq(
            "ukNonFhlProperty/0/countryCode"))), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendUkPropertyRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockAmendUkPropertyService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear, businessId, submissionId)(fakePostRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (RuleDuplicateSubmission, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}