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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockCreateHistoricNonFhlUkPiePeriodSummaryRequestParser
import v2.mocks.services.{ MockCreateHistoricNonFhlUkPiePeriodSummaryService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukPropertyRentARoom.{UkPropertyExpensesRentARoom, UkPropertyIncomeRentARoom}
import v2.models.request.createHistoricNonFhlUkPropertyPeriodSummary.{CreateHistoricNonFhlUkPropertyPeriodSummaryRawData, CreateHistoricNonFhlUkPropertyPeriodSummaryRequest, CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody, UkNonFhlPropertyExpenses, UkNonFhlPropertyIncome}
import v2.models.response.createHistoricNonFhlUkPiePeriodSummary.{CreateHistoricNonFhlUkPiePeriodSummaryHateoasData, CreateHistoricNonFhlUkPiePeriodSummaryResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateHistoricNonFhlUkPiePeriodSummaryControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateHistoricNonFhlUkPiePeriodSummaryService
    with MockCreateHistoricNonFhlUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino                 = "AA123456A"
  private val periodId             = "2019-03-11_2020-04-23"
  private val transactionId        = "0000000000000001"
  private val correlationId        = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val incomeModel = UkNonFhlPropertyIncome(
    periodAmount= Some(123.45),
    premiumsOfLeaseGrant= Some(2355.45),
    reversePremiums= Some(454.56),
    otherIncome= Some(567.89),
    taxDeducted= Some(9234.53),
    rentARoom = Some(UkPropertyIncomeRentARoom(Some(567.56)))
    )
  private val expensesModel = UkNonFhlPropertyExpenses(
    premisesRunningCosts = Some(567.53),
    repairsAndMaintenance = Some(324.65),
    financialCosts = Some(453.56),
    professionalFees = Some(535.78),
    costOfServices = Some(678.34),
    other = Some(682.34),
    travelCosts = Some(645.56),
    residentialFinancialCostsCarriedForward = Some(672.34),
    residentialFinancialCost = Some(1000.45),
    rentARoom = Some(UkPropertyExpensesRentARoom(Some(545.9))),
    consolidatedExpenses = None
  )


  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateHistoricNonFHLUkPiePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateHistoricNonFhlUkPiePeriodSummaryService,
      parser = mockCreateHistoricNonFhlUkPiePeriodSummaryRequestParser,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  private val requestBody =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody(fromDate = "2019-03-11", toDate = "2020-04-23", Some(incomeModel), Some(expensesModel))

  private val requestBodyJson = Json.parse(
    """{
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

  private val requestData =
    CreateHistoricNonFhlUkPropertyPeriodSummaryRequest(nino = Nino(nino), body = requestBody)
  private val rawData = CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(nino = nino, body = requestBodyJson)

  private val testHateoasLinks =
    Seq(Link(href = "/some/link", method = GET, rel = "someRel"))

  private val hateoasResponse = Json.parse(
    s"""{
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
       |}""".stripMargin
  )

  private val response = CreateHistoricNonFhlUkPiePeriodSummaryResponse(transactionId, Some(periodId))

  "create" should {
    "return a successful response" when {
      "the request received is valid" in new Test {
        MockCreateHistoricNonFhlUkPiePeriodSummaryRequestParser
          .requestFor(CreateHistoricNonFhlUkPropertyPeriodSummaryRawData(nino = nino, body = requestBodyJson))
          .returns(Right(requestData))

        MockCreateHistoricNonFhlUkPiePeriodSummaryService
          .createPeriodSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response,
            CreateHistoricNonFhlUkPiePeriodSummaryHateoasData( nino, periodId, transactionId))
          .returns(HateoasWrapper(response, testHateoasLinks))

        val result: Future[Result] = controller.handleRequest(nino)(fakeRequestWithBody(requestBodyJson))

        contentAsJson(result) shouldBe hateoasResponse
        status(result) shouldBe CREATED
        header("X-CorrelationId", result) shouldBe Some(correlationId)

      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {
            MockCreateHistoricNonFhlUkPiePeriodSummaryRequestParser
              .requestFor(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] =
              controller.handleRequest(nino = nino)(fakeRequestWithBody(requestBodyJson))

            contentAsJson(result) shouldBe Json.toJson(error)
            status(result) shouldBe expectedStatus
            header("X-CorrelationId", result) shouldBe Some(correlationId)

          }
        }

        val paths = Some(Seq("somePath"))
        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (RuleBothExpensesSuppliedError.copy(paths = paths), BAD_REQUEST),
          (ValueFormatError.copy(paths = paths), BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError.copy(paths = paths), BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (RuleToDateBeforeFromDateError, BAD_REQUEST)
        )
        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateHistoricNonFhlUkPiePeriodSummaryRequestParser
              .requestFor(rawData)
              .returns(Right(requestData))

            MockCreateHistoricNonFhlUkPiePeriodSummaryService
              .createPeriodSummary(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] =
              controller.handleRequest(nino = nino)(fakeRequestWithBody(requestBodyJson))

            contentAsJson(result) shouldBe Json.toJson(mtdError)
            status(result) shouldBe expectedStatus
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (RuleDuplicateSubmissionError, BAD_REQUEST),
          (RuleMisalignedPeriodError, BAD_REQUEST),
          (RuleOverlappingPeriodError, BAD_REQUEST),
          (RuleNotContiguousPeriodError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (InternalError, INTERNAL_SERVER_ERROR),
          (ServiceUnavailableError, INTERNAL_SERVER_ERROR)
        )
        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
