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
import api.hateoas.{HateoasWrapper, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, PeriodId}
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleMisalignedPeriodError}
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import mocks.MockIdGenerator
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Result
import v2.mocks.requestParsers.MockAmendHistoricNonFhlUkPiePeriodSummaryRequestParser
import v2.mocks.services.MockAmendHistoricNonFhlUkPropertyPeriodSummaryService
import v2.models.request.amendHistoricNonFhlUkPiePeriodSummary._
import v2.models.response.amendHistoricNonFhlUkPiePeriodSummary.AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendHistoricNonFhlUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendHistoricNonFhlUkPropertyPeriodSummaryService
    with MockAmendHistoricNonFhlUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator
    with MockAuditService {

  private val periodId      = "somePeriodId"
  private val mtdId: String = "test-mtd-id"

  "AmendHistoricNonFhlUkPropertyPeriodSummaryController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendHistoricNonFhlUkPropertyPeriodSummaryService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), hateoasData)
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(testHateoasLinksJson))
      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockAmendHistoricNonFhlUkPiePeriodSummaryRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendHistoricNonFhlUkPropertyPeriodSummaryService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    private val controller = new AmendHistoricNonFhlUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockAmendHistoricNonFhlUkPropertyPeriodSummaryRequestParser,
      service = mockAmendHistoricNonFhlUkPropertyPeriodSummaryService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, periodId)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "AmendHistoricFhlPropertyIncomeExpensesPeriodSummary",
        transactionName = "AmendHistoricFhlPropertyIncomeExpensesPeriodSummary",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some("2.0"),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino, "periodId" -> periodId),
          request = Some(requestBodyJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBodyJson: JsValue = JsObject.empty

    protected val rawData: AmendHistoricNonFhlUkPiePeriodSummaryRawData =
      AmendHistoricNonFhlUkPiePeriodSummaryRawData(nino, periodId, requestBodyJson)

    protected val requestBody: AmendHistoricNonFhlUkPiePeriodSummaryRequestBody = AmendHistoricNonFhlUkPiePeriodSummaryRequestBody(None, None)

    protected val requestData: AmendHistoricNonFhlUkPiePeriodSummaryRequest =
      AmendHistoricNonFhlUkPiePeriodSummaryRequest(Nino(nino), PeriodId(periodId), requestBody)

    protected val hateoasData: AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData =
      AmendHistoricNonFhlUkPropertyPeriodSummaryHateoasData(nino, periodId)

  }

}
