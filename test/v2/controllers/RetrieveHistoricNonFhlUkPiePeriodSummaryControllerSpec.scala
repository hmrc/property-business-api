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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.{Nino, PeriodId}
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import api.models.hateoas.HateoasWrapper
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import v2.mocks.requestParsers.MockRetrieveHistoricNonFhlUkPiePeriodSummaryRequestParser
import v2.mocks.services.MockRetrieveHistoricNonFhlUkPiePeriodSummaryService
import v2.models.request.retrieveHistoricNonFhlUkPiePeriodSummary._
import v2.models.response.retrieveHistoricNonFhlUkPiePeriodSummary._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPiePeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveHistoricNonFhlUkPiePeriodSummaryService
    with MockRetrieveHistoricNonFhlUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val from     = "2017-04-06"
  private val to       = "2017-07-04"
  private val periodId = s"${from}_$to"

  "RetrieveHistoricNonFhlUkPiePeriodSummaryController" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockRetrieveHistoricNonFhlUkPiePeriodSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveHistoricNonFhlUkPiePeriodSummaryService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBodyJsonWithHateoas))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveHistoricNonFhlUkPiePeriodSummaryRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveHistoricNonFhlUkPiePeriodSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveHistoricNonFhlUkPiePeriodSummaryService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveHistoricNonFhlUkPiePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveHistoricNonFhlUkPiePeriodSummaryRequestParser,
      service = mockRetrieveHistoricNonFhlUkPiePeriodSummaryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, periodId)(fakeGetRequest)

    protected val rawData: RetrieveHistoricNonFhlUkPiePeriodSummaryRawData = RetrieveHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId)

    protected val requestData: RetrieveHistoricNonFhlUkPiePeriodSummaryRequest =
      RetrieveHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId))

    private val periodIncome: PeriodIncome =
      PeriodIncome(Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99), Some(RentARoomIncome(Some(5000.99))))

    //@formatter:off
    private val periodExpenses: PeriodExpenses = PeriodExpenses(
      Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99),
      None, Some(5000.99), Some(5000.99), Some(5000.99), Some(RentARoomExpenses(Some(5000.99)))
    )
    //@formatter:on

    protected val responseData: RetrieveHistoricNonFhlUkPiePeriodSummaryResponse = RetrieveHistoricNonFhlUkPiePeriodSummaryResponse(
      fromDate = from,
      toDate = to,
      Some(periodIncome),
      Some(periodExpenses)
    )

    protected val hateoasData: RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData =
      RetrieveHistoricNonFhlUkPiePeriodSummaryHateoasData(nino, periodId)

    private val responseBodyJson: JsValue = Json.parse("""
                                                         |{ 
                                                         |  "fromDate": "2017-04-06",
                                                         |  "toDate":"2017-07-04",
                                                         |  "income": {
                                                         |      "periodAmount": 5000.99,
                                                         |      "premiumsOfLeaseGrant": 5000.99,
                                                         |      "reversePremiums": 5000.99,
                                                         |      "otherIncome": 5000.99,
                                                         |      "taxDeducted": 5000.99,
                                                         |      "rentARoom": {
                                                         |        "rentsReceived":5000.99
                                                         |       }
                                                         |   },
                                                         |   "expenses": {
                                                         |      "premisesRunningCosts": 5000.99,
                                                         |      "repairsAndMaintenance": 5000.99,
                                                         |      "financialCosts": 5000.99,
                                                         |      "professionalFees": 5000.99,
                                                         |      "costOfServices": 5000.99,
                                                         |      "other": 5000.99,
                                                         |      "travelCosts": 5000.99,
                                                         |      "residentialFinancialCost": 5000.99,
                                                         |      "residentialFinancialCostsCarriedForward": 5000.99,
                                                         |      "rentARoom": {
                                                         |          "amountClaimed": 5000.99
                                                         |      }
                                                         |  }
                                                         |}
                                                         |""".stripMargin)

    protected val responseBodyJsonWithHateoas: JsObject = responseBodyJson.as[JsObject] ++ testHateoasLinksJson
  }

}
