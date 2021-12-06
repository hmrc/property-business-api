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
import v2.mocks.requestParsers.MockAmendForeignPropertyPeriodSummaryRequestParser
import v2.mocks.services._
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.hateoas.Method.GET
import v2.models.hateoas.{HateoasWrapper, Link}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendForeignPropertyPeriodSummary._
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._
import v2.models.response.amendForeignPropertyPeriodSummary._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendForeignPropertyPeriodSummaryService
    with MockAmendForeignPropertyPeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino = "AA123456A"
  private val businessId = "XAIS12345678910"
  private val taxYear = "2020-21"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendForeignPropertyPeriodSummaryRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)
  }

  val requestBody: AmendForeignPropertyPeriodSummaryRequestBody =
    AmendForeignPropertyPeriodSummaryRequestBody(
      Some(AmendForeignFhlEea(
        Some(ForeignFhlEeaIncome(
          Some(1000.12)
        )),
        Some(AmendForeignFhlEeaExpenses(
          Some(1000.23),
          Some(1000.34),
          Some(1000.45),
          Some(1000.56),
          Some(1000.67),
          Some(1000.78),
          Some(1000.89),
          None
        ))
      )),
      Some(Seq(AmendForeignNonFhlPropertyEntry(
        countryCode = "ZZZ",
        Some(ForeignNonFhlPropertyIncome(
          Some(ForeignNonFhlPropertyRentIncome(Some(2000.12))),
          true,
          Some(2000.23),
          Some(2000.34),
          Some(2000.45),
          Some(2000.56)
        )),
        expenses = Some(AmendForeignNonFhlPropertyExpenses(
          Some(3000.01),
          Some(3000.12),
          Some(3000.23),
          Some(3000.34),
          Some(3000.45),
          Some(3000.56),
          Some(3000.67),
          Some(3000.78),
          Some(3000.89),
          None
        ))
      )))
    )

  val requestBodyWithConsolidatedExpense: AmendForeignPropertyPeriodSummaryRequestBody =
    AmendForeignPropertyPeriodSummaryRequestBody(
      Some(AmendForeignFhlEea(
        Some(ForeignFhlEeaIncome(
          Some(1000.12)
        )),
        Some(AmendForeignFhlEeaExpenses(
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          Some(1000.23)
        ))
      )),
      Some(Seq(AmendForeignNonFhlPropertyEntry(
        countryCode = "ZZZ",
        Some(ForeignNonFhlPropertyIncome(
          Some(ForeignNonFhlPropertyRentIncome(Some(2000.12))),
          true,
          Some(2000.23),
          Some(2000.34),
          Some(2000.45),
          Some(2000.56)
        )),
        expenses = Some(AmendForeignNonFhlPropertyExpenses(
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          Some(2000.67)
        ))
      )))
    )

  private val requestBodyJson = Json.parse(
    """{
      |    "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 1123.89
      |    },
      |    "expenses": {
      |      "premisesRunningCosts": 332.78,
      |      "repairsAndMaintenance": 231.45,
      |      "financialCosts": 345.23,
      |      "professionalFees": 232.45,
      |      "costOfServices": 231.56,
      |      "travelCosts": 234.67,
      |      "other": 3457.9
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "AFG",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 440.31
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 950.48,
      |        "otherPropertyIncome": 802.49,
      |        "foreignTaxPaidOrDeducted": 734.18,
      |        "specialWithholdingTaxOrUkTaxPaid": 85.47
      |      },
      |      "expenses": {
      |        "premisesRunningCosts":129.35,
      |        "repairsAndMaintenance":7490.32,
      |        "financialCosts":5000.99,
      |        "professionalFees":847.90,
      |        "travelCosts":69.20,
      |        "costOfServices":478.23,
      |        "residentialFinancialCost":879.28,
      |        "broughtFwdResidentialFinancialCost":846.13,
      |        "other":138.92
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin
  )
  private val requestBodyJsonConsolidatedExpenses = Json.parse(
    """{
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 1123.89
      |    },
      |    "expenses": {
      |      "consolidatedExpenses": 334.64
      |    }
      |  },
      |  "foreignNonFhlProperty": [
      |    {
      |      "countryCode": "AFG",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 440.31
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumsOfLeaseGrant": 950.48,
      |        "otherPropertyIncome": 802.49,
      |        "foreignTaxPaidOrDeducted": 734.18,
      |        "specialWithholdingTaxOrUkTaxPaid": 85.47
      |      },
      |      "expenses": {
      |        "consolidatedExpenses": 3992.93,
      |        "residentialFinancialCost":879.28,
      |        "broughtFwdResidentialFinancialCost":846.13
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  private val requestData = AmendForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, taxYear, submissionId, requestBody)
  private val rawData = AmendForeignPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId, requestBodyJson)

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |  "links": [
       |    {
       |      "href":"/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId",
       |      "method":"GET",
       |      "rel":"self"
       |    }
       |  ]
       |}
    """.stripMargin
  )

  private val testHateoasLink =
    Link(href = s"/individuals/business/property/foreign/$nino/$businessId/period/$taxYear/$submissionId",
      method = GET, rel="self")

    "amend" should {
      "return a successful response from a consolidated request" when {
        "the request received is valid" in new Test {

          MockAmendForeignPropertyPeriodSummaryRequestParser
            .requestFor(AmendForeignPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId, requestBodyJsonConsolidatedExpenses))
            .returns(Right(requestData))

          MockAmendForeignPropertyPeriodSummaryService
            .amend(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

          MockHateoasFactory
            .wrap((), AmendForeignPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))
            .returns(HateoasWrapper((), Seq(testHateoasLink)))

          val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakePostRequest(requestBodyJsonConsolidatedExpenses))
          status(result) shouldBe OK
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }
      }

      "return a successful response from an unconsolidated request" when {
        "the request received is valid" in new Test {

          MockAmendForeignPropertyPeriodSummaryRequestParser
            .requestFor(AmendForeignPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId, requestBodyJson))
            .returns(Right(requestData))

          MockAmendForeignPropertyPeriodSummaryService
            .amend(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

          MockHateoasFactory
            .wrap((), AmendForeignPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId))
            .returns(HateoasWrapper((), Seq(testHateoasLink)))

          val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakePostRequest(requestBodyJson))
          status(result) shouldBe OK
          header("X-CorrelationId", result) shouldBe Some(correlationId)

        }
      }

    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendForeignPropertyPeriodSummaryRequestParser
              .requestFor(rawData.copy(body = requestBodyJsonConsolidatedExpenses))
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakePostRequest(requestBodyJsonConsolidatedExpenses))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (SubmissionIdFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (RuleBothExpensesSuppliedError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (RuleDuplicateCountryCodeError, BAD_REQUEST),
          (CountryCodeFormatError, BAD_REQUEST),
          (RuleCountryCodeError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendForeignPropertyPeriodSummaryRequestParser
              .requestFor(rawData)
              .returns(Right(requestData))

            MockAmendForeignPropertyPeriodSummaryService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakePostRequest(requestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (SubmissionIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (RuleTypeOfBusinessIncorrectError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleDuplicateCountryCodeError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
