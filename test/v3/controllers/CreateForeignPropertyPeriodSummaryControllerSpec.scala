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

package v3.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.{HateoasWrapper, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.AppConfig
import mocks.{MockAppConfig, MockIdGenerator}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import v3.controllers.validators.MockCreateForeignPropertyPeriodSummaryValidatorFactory
import v3.fixtures.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryFixtures
import v3.models.request.createForeignPropertyPeriodSummary.CreateForeignPropertyPeriodSummaryRequestData
import v3.models.response.createForeignPropertyPeriodSummary._
import v3.services.MockCreateForeignPropertyPeriodSummaryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateForeignPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateForeignPropertyPeriodSummaryService
    with MockCreateForeignPropertyPeriodSummaryValidatorFactory
    with MockAuditService
    with MockHateoasFactory
    with MockIdGenerator
    with CreateForeignPropertyPeriodSummaryFixtures {

  private val taxYear               = "2020-21"
  private val businessId            = "XAIS12345678910"
  private val submissionId          = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val appConfig: AppConfig = mockAppConfig

  "CreateForeignPropertyPeriodSummaryControllerSpec" should {
    "return a successful response with status 201 (CREATED)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedCreateForeignPropertyPeriodSummaryService
          .createForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(
            response,
            CreateForeignPropertyPeriodSummaryHateoasData(nino = nino, businessId = businessId, taxYear = taxYear, submissionId = submissionId))
          .returns(HateoasWrapper(response, testHateoasLinks))

        runOkTest(expectedStatus = CREATED, maybeExpectedResponseBody = Some(hateoasResponse))

      }

      "return the error as per spec" when {
        "the parser validation fails" in new Test {
          willUseValidator(returning(NinoFormatError))

          runErrorTest(NinoFormatError)
        }

        "the service returns an error" in new Test {
          willUseValidator(returningSuccess(requestData))

          MockedCreateForeignPropertyPeriodSummaryService
            .createForeignProperty(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

          runErrorTest(RuleMisalignedPeriodError)
        }
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new CreateForeignPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateForeignPropertyPeriodSummaryService,
      validatorFactory = mockCreateForeignPropertyPeriodSummaryValidatorFactory,
      auditService = mockAuditService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakePostRequest(requestBody))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateForeignPropertyIncomeAndExpensesPeriodSummary",
        transactionName = "create-foreign-property-income-and-expenses-period-summary",
        detail = GenericAuditDetail(
          versionNumber = "3.0",
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear, "businessId" -> businessId),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    val requestBody: JsObject = JsObject.empty

    protected val requestData: CreateForeignPropertyPeriodSummaryRequestData =
      CreateForeignPropertyPeriodSummaryRequestData(
        nino = Nino(nino),
        businessId = BusinessId(businessId),
        taxYear = TaxYear.fromMtd(taxYear),
        body = regularExpensesRequestBody)

    protected val hateoasResponse: JsObject = Json
      .parse(
        s"""
           |{
           |  "submissionId": "$submissionId"
           |}
      """.stripMargin
      )
      .as[JsObject] ++ testHateoasLinksJson

    protected val response: CreateForeignPropertyPeriodSummaryResponse = CreateForeignPropertyPeriodSummaryResponse(submissionId)
  }

}
