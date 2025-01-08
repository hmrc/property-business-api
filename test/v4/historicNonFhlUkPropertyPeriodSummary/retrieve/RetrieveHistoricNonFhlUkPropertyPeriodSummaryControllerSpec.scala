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

package v4.historicNonFhlUkPropertyPeriodSummary.retrieve

import common.models.domain.PeriodId
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.Nino
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v4.historicNonFhlUkPropertyPeriodSummary.retrieve.def1.model.response.{PeriodExpenses, PeriodIncome, RentARoomExpenses, RentARoomIncome}
import v4.historicNonFhlUkPropertyPeriodSummary.retrieve.model.request.{
  Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData
}
import v4.historicNonFhlUkPropertyPeriodSummary.retrieve.model.response._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryService
    with MockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory
    with MockAuditService
    with MockIdGenerator {

  private val from     = "2017-04-06"
  private val to       = "2017-07-04"
  private val periodId = s"${from}_$to"

  "RetrieveHistoricNonFhlUkPropertyPeriodSummaryController" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveHistoricNonFhlUkPropertyPeriodSummaryService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseBodyJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveHistoricNonFhlUkPropertyPeriodSummaryService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller = new RetrieveHistoricNonFhlUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory,
      service = mockRetrieveHistoricNonFhlUkPropertyPeriodSummaryService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, periodId)(fakeGetRequest)

    protected val requestData: RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData =
      Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryRequestData(Nino(validNino), PeriodId(periodId))

    private val periodIncome: PeriodIncome =
      PeriodIncome(Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99), Some(RentARoomIncome(Some(5000.99))))

    //@formatter:off
    private val periodExpenses: PeriodExpenses = PeriodExpenses(
      Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99), Some(5000.99),
      None, Some(5000.99), Some(5000.99), Some(5000.99), Some(RentARoomExpenses(Some(5000.99)))
    )
    //@formatter:on

    protected val responseData: RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse =
      Def1_RetrieveHistoricNonFhlUkPropertyPeriodSummaryResponse(
        fromDate = from,
        toDate = to,
        Some(periodIncome),
        Some(periodExpenses)
      )

    protected val responseBodyJson: JsValue = Json.parse("""
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

  }

}
