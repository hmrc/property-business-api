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
import v2.mocks.requestParsers.MockRetrieveHistoricFhlUkPiePeriodSummaryRequestParser
import v2.mocks.services.MockRetrieveHistoricFhlUkPiePeriodSummaryService
import v2.models.request.retrieveHistoricFhlUkPiePeriodSummary._
import v2.models.response.retrieveHistoricFhlUkPiePeriodSummary._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPiePeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveHistoricFhlUkPiePeriodSummaryService
    with MockRetrieveHistoricFhlUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val from     = "2017-04-06"
  private val to       = "2017-07-04"
  private val periodId = s"${from}_$to"

  "RetrieveHistoricFhlUkPiePeriodSummaryController" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockRetrieveHistoricFhlUkPiePeriodSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveHistoricFhlUkPiePeriodSummaryService
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
        MockRetrieveHistoricFhlUkPiePeriodSummaryRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveHistoricFhlUkPiePeriodSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveHistoricFhlUkPiePeriodSummaryService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    private val controller = new RetrieveHistoricFhlUkPiePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveHistoricFhlUkPiePeriodSummaryRequestParser,
      service = mockRetrieveHistoricFhlUkPiePeriodSummaryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, periodId)(fakeGetRequest)

    protected val rawData: RetrieveHistoricFhlUkPiePeriodSummaryRawData = RetrieveHistoricFhlUkPiePeriodSummaryRawData(nino, periodId)

    protected val requestData: RetrieveHistoricFhlUkPiePeriodSummaryRequest =
      RetrieveHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId))

    private val periodIncome: PeriodIncome = PeriodIncome(Some(5000.99), Some(5000.99), Some(RentARoomIncome(Some(5000.99))))

    private val periodExpenses: PeriodExpenses = PeriodExpenses(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      None,
      Some(5000.99),
      Some(RentARoomExpenses(Some(5000.99))))

    protected val responseData: RetrieveHistoricFhlUkPiePeriodSummaryResponse = RetrieveHistoricFhlUkPiePeriodSummaryResponse(
      fromDate = from,
      toDate = to,
      Some(periodIncome),
      Some(periodExpenses)
    )

    protected val hateoasData: RetrieveHistoricFhlUkPiePeriodSummaryHateoasData = RetrieveHistoricFhlUkPiePeriodSummaryHateoasData(nino, periodId)

    private val responseBodyJson: JsValue = Json.parse("""
       |{
       |  "fromDate": "2017-04-06",
       |  "toDate":"2017-07-04",
       |  "income": {
       |    "periodAmount":5000.99,
       |    "taxDeducted":5000.99,
       |    "rentARoom": {
       |      "rentsReceived":5000.99
       |    }
       |  },
       |  "expenses": {
       |    "premisesRunningCosts":5000.99,
       |    "repairsAndMaintenance":5000.99,
       |    "financialCosts":5000.99,
       |    "professionalFees":5000.99,
       |    "costOfServices":5000.99,
       |    "other":5000.99,
       |    "travelCosts":5000.99,
       |    "rentARoom": {
       |      "amountClaimed":5000.99
       |     }
     |     }
       | }
       |""".stripMargin)

    protected val responseBodyJsonWithHateoas: JsObject = responseBodyJson.as[JsObject] ++ testHateoasLinksJson
  }

}
