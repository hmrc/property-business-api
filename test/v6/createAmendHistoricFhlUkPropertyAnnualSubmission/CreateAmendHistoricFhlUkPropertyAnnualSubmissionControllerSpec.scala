/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.createAmendHistoricFhlUkPropertyAnnualSubmission

import common.models.errors.RuleMisalignedPeriodError
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.auth.UserDetails
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v6.createAmendHistoricFhlUkPropertyAnnualSubmission.model.request._
import v6.createAmendHistoricFhlUkPropertyAnnualSubmission.model.response._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionService
    with MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionValidatorFactory
    with MockIdGenerator
    with MockAuditService {

  private val taxYear                            = "2022-23"
  private val transactionReference               = Some("transaction reference")
  private val mtdId: String                      = "test-mtd-id"
  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  "CreateAmendHistoricFhlUkPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = OK)
      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    protected val controller = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAmendHistoricFhlUkPropertyAnnualSubmissionValidatorFactory,
      service = mockCreateAmendHistoricFhlUkPropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, taxYear)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission",
        transactionName = "CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission",
        detail = GenericAuditDetail(
          userDetails = UserDetails(mtdId, "Individual", None),
          apiVersion = apiVersion.name,
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = Some(validMtdJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBodyJson: JsValue = JsObject.empty

    protected val requestBody: Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody =
      Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(None, None)

    protected val requestData: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestData(Nino(validNino), TaxYear.fromMtd(taxYear), requestBody)

    protected val responseData: CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse =
      CreateAmendHistoricFhlUkPropertyAnnualSubmissionResponse(transactionReference)

    protected val validMtdJson: JsValue = Json.parse(
      """
        |{
        |   "annualAdjustments": {
        |      "lossBroughtForward": 200.00,
        |      "balancingCharge": 200.00,
        |      "privateUseAdjustment": 200.00,
        |      "periodOfGraceAdjustment": true,
        |      "businessPremisesRenovationAllowanceBalancingCharges": 200.02,
        |      "nonResidentLandlord": true,
        |      "rentARoom": {
        |         "jointlyLet": true
        |      }
        |   },
        |   "annualAllowances": {
        |      "annualInvestmentAllowance": 200.00,
        |      "otherCapitalAllowance": 200.00,
        |      "businessPremisesRenovationAllowance": 100.02,
        |      "propertyIncomeAllowance": 10.02
        |   }
        |}
        |""".stripMargin
    )

  }

}
