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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockListForeignPropertiesRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockListForeignPropertiesService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.Method.GET
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listForeignProperties.{ListForeignPropertiesRawData, ListForeignPropertiesRequest}
import v1.models.response.listForeignProperties.{ListForeignPropertiesHateoasData, ListForeignPropertiesResponse, SubmissionPeriod}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListForeignPropertiesControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListForeignPropertiesService
    with MockListForeignPropertiesRequestParser
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new ListForeignPropertiesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockListForeignPropertiesRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  val nino = "AA123456A"
  val businessId = "XAIS12345678910"
  val fromDate = "2020-06-01"
  val toDate = "2020-08-31"
  private val correlationId = "X-123"

  val request = ListForeignPropertiesRequest(nino, businessId, fromDate, toDate)

  val response = ListForeignPropertiesResponse(Seq(
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22"),
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3d", "2020-08-22", "2020-08-22")
  ))

  private val testHateoasLink = Link(href = s"Individuals/business/property/$nino/$businessId/period", method = GET, rel = "self")

  private val responseJson = Json.parse(
    """|{
       |  "foreignFhlEea": {
       |    "income": {
       |      "rentAmount": 567.83,
       |      "taxDeducted": 4321.92
       |      },
       |    "expenditure": {
       |      "premisesRunningCosts": 4567.98,
       |      "repairsAndMaintenance": 98765.67,
       |      "financialCosts": 4566.95,
       |      "professionalFees": 23.65,
       |      "costsOfServices": 4567.77,
       |      "travelCosts": 456.77,
       |      "other": 567.67
       |    }
       |  },
       |  "foreignProperty": [{
       |      "countryCode": "zzz",
       |      "income": {
       |        "rentIncome": {
       |          "rentAmount": 34456.30,
       |          "taxDeducted": 6334.34
       |        },
       |        "foreignTaxCreditRelief": true,
       |        "premiumOfLeaseGrant": 2543.43,
       |        "otherPropertyIncome": 54325.30,
       |        "foreignTaxTakenOff": 6543.01,
       |        "specialWithholdingTaxOrUKTaxPaid": 643245.00
       |      },
       |      "expenditure": {
       |        "premisesRunningCosts": 5635.43,
       |        "repairsAndMaintenance": 3456.65,
       |        "financialCosts": 34532.21,
       |        "professionalFees": 32465.32,
       |        "costsOfServices": 2567.21,
       |        "travelCosts": 2345.76,
       |        "residentialFinancialCost": 21235.22,
       |        "broughtFwdResidentialFinancialCost": 12556.00,
       |        "other": 2425.11
       |      }
       |    }
       |  ]
       |}
       |""".stripMargin)


  private val rawData = ListForeignPropertiesRawData(nino, businessId, fromDate, toDate)
  private val requestData = ListForeignPropertiesRequest(Nino(nino), businessId, fromDate, toDate)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockListForeignPropertiesRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockListForeignPropertiesService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseJson)))

        MockHateoasFactory
          .wrap((), ListForeignPropertiesHateoasData(nino, businessId))
          .returns(HateoasWrapper((), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, businessId, fromDate, toDate)(fakeGetRequest)
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListForeignPropertiesRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, fromDate, toDate)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          ???
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockListForeignPropertiesRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockListForeignPropertiesService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId, fromDate, toDate)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}