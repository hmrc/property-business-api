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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.Link
import v1.models.outcomes.ResponseWrapper

import scala.concurrent.Future

class AmendForeignPropertyControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendForeignPropertyService
    with MockAmendForeignPropertyRequestParser
    with MockHateoasFactory
    with MockAuditService {

  trait Test {
    val hc = HeaderCarrier()

    val controller = new AmendForeignPropertyController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendForeignPropertyRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  private val testHateoasLink = Link(href = s"individuals/other/deductions/$nino/$taxYear", method = PUT, rel = "self")

  private val requestJson = Json.parse(
    """|
       |{
       |  "seafarers":[
       |    {
       |      "customerReference": "myRef",
       |      "amountDeducted": 2342.22,
       |      "nameOfShip": "Blue Bell",
       |      "fromDate": "2018-08-17",
       |      "toDate":"2018-10-02"
       |    }
       |  ]
       |}
       |""".stripMargin
  )

  private val requestBody = AmendForeignPropertyBody(
    Some(Seq(Seafarers(
      Some("myRef"),
      2342.22,
      "Blue Bell",
      "2018-08-17",
      "2018-10-02"
    )))
  )



  private val rawData = AmendForeignPropertyRawData(nino, taxYear, requestJson)
  private val requestData = AmendForeignPropertyRequest(Nino(nino), taxYear, requestBody)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockAmendForeignPropertyRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockAmendForeignPropertyService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), AmendForeignPropertyHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), Seq(testHateoasLink)))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendForeignPropertyRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (ValueFormatError.copy(paths = Some(Seq(
            "seafarers/0/amountDeducted",
            "seafarers/1/amountDeducted"))), BAD_REQUEST),
          (NameOfShipFormatError.copy(paths = Some(Seq(
            "seafarers/0/nameOfShip",
            "seafarers/1/nameOfShip"))), BAD_REQUEST),
          (CustomerReferenceFormatError.copy(paths = Some(Seq(
            "seafarers/0/customerReference",
            "seafarers/1/customerReference"))), BAD_REQUEST),
          (DateFormatError.copy(paths = Some(Seq(
            "seafarers/0/fromDate",
            "seafarers/0/toDate",
            "seafarers/1/fromDate",
            "seafarers/1/toDate"))), BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RangeToDateBeforeFromDateError.copy(paths = Some(Seq(
            "seafarers/0/fromDate",
            "seafarers/0/toDate",
            "seafarers/1/fromDate",
            "seafarers/1/toDate"))), BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendForeignPropertyRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockAmendForeignPropertyService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR),
          (TaxYearFormatError, BAD_REQUEST)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}