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

package v4.historicNonFhlUkPropertyPeriodSummary.create

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, PeriodId}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import utils.MockIdGenerator
import v4.historicNonFhlUkPropertyPeriodSummary.create.model.request.{
  CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData,
  Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody,
  Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData
}
import v4.historicNonFhlUkPropertyPeriodSummary.create.model.response.CreateHistoricNonFhlUkPropertyPeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateHistoricNonFhlUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateHistoricNonFhlUkPropertyPeriodSummaryService
    with MockCreateHistoricNonFhlUkPropertyPeriodSummaryValidatorFactory
    with MockIdGenerator
    with MockAuditService {

  private val periodId      = "2021-01-01_2021-01-02"
  private val mtdId: String = "test-mtd-id"

  "CreateHistoricNonFhlUkPiePeriodSummaryController" should {
    "return a successful response with status 201 (CREATED)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedCreateHistoricNonFhlUkPropertyPeriodSummaryService
          .createPeriodSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(responseBodyJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedCreateHistoricNonFhlUkPropertyPeriodSummaryService
          .createPeriodSummary(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    protected val controller: CreateHistoricNonFhlUkPropertyPeriodSummaryController = new CreateHistoricNonFhlUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateHistoricNonFhlUkPiePeriodSummaryValidatorFactory,
      service = mockCreateHistoricNonFhlUkPropertyPeriodSummaryService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(nino)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "CreateHistoricNonFhlPropertyIncomeExpensesPeriodSummary",
        transactionName = "create-historic-non-fhl-property-income-expenses-period-summary",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some(apiVersion.name),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino),
          request = Some(requestBodyJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBodyJson: JsValue = Json.parse("""
      |{
      |    "fromDate": "2021-01-01",
      |    "toDate": "2021-01-02"
      |}
      |""".stripMargin)

    protected val responseBodyJson: JsValue = Json.parse("""
      |{
      |   "periodId": "2021-01-01_2021-01-02"
      |}
      |""".stripMargin)

    protected val requestBody: Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody =
      Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestBody("2021-01-01", "2021-01-02", None, None)

    protected val requestData: CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData =
      Def1_CreateHistoricNonFhlUkPropertyPeriodSummaryRequestData(Nino(nino), requestBody)

    protected val responseData: CreateHistoricNonFhlUkPropertyPeriodSummaryResponse = CreateHistoricNonFhlUkPropertyPeriodSummaryResponse(
      PeriodId(periodId))

  }

}
