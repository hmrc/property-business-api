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

package v4.historicFhlUkPropertyPeriodSummary.create

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, PeriodId}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import mocks.MockIdGenerator
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v4.historicFhlUkPropertyPeriodSummary.create.model.request.{
  CreateHistoricFhlUkPropertyPeriodSummaryRequestData,
  Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody,
  Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData
}
import v4.historicFhlUkPropertyPeriodSummary.create.model.response.CreateHistoricFhlUkPropertyPeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateHistoricFhlUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateHistoricFhlUkPropertyPeriodSummaryService
    with MockCreateHistoricFhlUkPropertyPeriodSummaryValidatorFactory
    with MockIdGenerator
    with MockAuditService {

  private val taxYear       = "2022-23"
  private val fromDate      = "2021-01-01"
  private val toDate        = "2021-01-02"
  private val mtdId: String = "test-mtd-id"

  "CreateCreateHistoricFhlUkPropertyAnnualSubmissionController" should {
    "return a successful response with status 201 (CREATED)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedCreateHistoricFhlUkPropertyPeriodSummaryService
          .create(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = CREATED, Some(responseBodyJson))
      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedCreateHistoricFhlUkPropertyPeriodSummaryService
          .create(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    private val controller = new CreateHistoricFhlUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateHistoricFhlUkPropertyPeriodSummaryValidatorFactory,
      service = mockCreateHistoricFhlUkPropertyPeriodSummaryService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAndCreateHistoricFhlPropertyBusinessAnnualSubmission",
        transactionName = "CreateAndCreateHistoricFhlPropertyBusinessAnnualSubmission",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some("2.0"),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = Some(responseBodyJson),
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

    protected val requestBody: Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody =
      Def1_CreateHistoricFhlUkPiePeriodSummaryRequestBody(fromDate, toDate, None, None)

    protected val requestData: CreateHistoricFhlUkPropertyPeriodSummaryRequestData =
      Def1_CreateHistoricFhlUkPropertyPeriodSummaryRequestData(Nino(nino), requestBody)

    protected val responseData: CreateHistoricFhlUkPropertyPeriodSummaryResponse =
      CreateHistoricFhlUkPropertyPeriodSummaryResponse(PeriodId(fromDate, toDate))

  }

}
