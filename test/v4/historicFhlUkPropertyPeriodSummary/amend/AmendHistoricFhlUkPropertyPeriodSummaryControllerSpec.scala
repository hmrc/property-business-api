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

package v4.historicFhlUkPropertyPeriodSummary.amend

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, PeriodId}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import mocks.{MockAppConfig, MockIdGenerator}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import v4.historicFhlUkPropertyPeriodSummary.amend.request.{
  AmendHistoricFhlUkPropertyPeriodSummaryRequestData,
  Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody,
  Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendHistoricFhlUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAmendHistoricFhlUkPropertyPeriodSummaryService
    with MockAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory
    with MockIdGenerator
    with MockAuditService {

  private val taxYear       = "2022-23"
  private val periodId      = PeriodId(from = "2017-04-06", to = "2017-07-04")
  private val mtdId: String = "test-mtd-id"

  "CreateAmendHistoricFhlUkPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockAmendHistoricFhlUkPropertyPeriodSummaryService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

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

        MockAmendHistoricFhlUkPropertyPeriodSummaryService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    private val controller = new AmendHistoricFhlUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockAmendHistoricFhlUkPropertyPeriodSummaryValidatorFactory,
      service = mockAmendHistoricFhlUkPropertyPeriodSummaryService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission",
        transactionName = "CreateAndAmendHistoricFhlPropertyBusinessAnnualSubmission",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some(apiVersion.name),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = Some(validMtdJson),
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

    private val requestBodyJson: JsValue = JsObject.empty

    protected val requestBody: Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody =
      Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestBody(None, None)

    protected val requestData: AmendHistoricFhlUkPropertyPeriodSummaryRequestData =
      Def1_AmendHistoricFhlUkPropertyPeriodSummaryRequestData(Nino(nino), periodId, requestBody)

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
