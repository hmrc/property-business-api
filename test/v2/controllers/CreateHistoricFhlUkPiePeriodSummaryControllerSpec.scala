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
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.AppConfig
import mocks.{MockAppConfig, MockIdGenerator}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import v2.controllers.validators.MockCreateHistoricFhlUkPiePeriodSummaryValidatorFactory
import v2.models.request.createHistoricFhlUkPiePeriodSummary._
import v2.models.response.createHistoricFhlUkPiePeriodSummary._
import v2.services.MockCreateHistoricFhlUkPiePeriodSummaryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateHistoricFhlUkPiePeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateHistoricFhlUkPiePeriodSummaryService
    with MockCreateHistoricFhlUkPiePeriodSummaryValidatorFactory
    with MockHateoasFactory
    with MockIdGenerator
    with MockAuditService {

  private val periodId              = "2021-01-01_2021-01-02"
  private val mtdId: String         = "test-mtd-id"
  implicit val appConfig: AppConfig = mockAppConfig

  "CreateHistoricFhlUkPiePeriodSummaryController" should {
    "return a successful response with status 201 (CREATED)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateHistoricFhlUkPiePeriodSummaryService
          .createPeriodSummary(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, testHateoasLinks))

        runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(responseBodyJsonWithHateoas))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateHistoricFhlUkPiePeriodSummaryService
          .createPeriodSummary(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    private val controller: CreateHistoricFhlUkPiePeriodSummaryController = new CreateHistoricFhlUkPiePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateHistoricFhlUkPiePeriodSummaryValidatorFactory,
      service = mockCreateHistoricFhlUkPiePeriodSummaryService,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "CreateHistoricFhlPropertyIncomeExpensesPeriodSummary",
        transactionName = "CreateHistoricFhlPropertyIncomeExpensesPeriodSummary",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some("2.0"),
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

    private val responseBodyJson: JsValue = Json.parse("""
        |{
        |   "periodId": "2021-01-01_2021-01-02"
        |}
        |""".stripMargin)

    protected val responseBodyJsonWithHateoas: JsObject = responseBodyJson.as[JsObject] ++ testHateoasLinksJson

    protected val requestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
      CreateHistoricFhlUkPiePeriodSummaryRequestBody("2021-01-01", "2021-01-02", None, None)

    protected val requestData: CreateHistoricFhlUkPiePeriodSummaryRequestData =
      CreateHistoricFhlUkPiePeriodSummaryRequestData(Nino(nino), requestBody)

    protected val hateoasData: CreateHistoricFhlUkPiePeriodSummaryHateoasData =
      CreateHistoricFhlUkPiePeriodSummaryHateoasData(nino, PeriodId(periodId))

    protected val responseData: CreateHistoricFhlUkPiePeriodSummaryResponse = CreateHistoricFhlUkPiePeriodSummaryResponse(PeriodId(periodId))

  }

}
