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

package v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import api.models.auth.UserDetails
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import mocks.{MockAppConfig, MockIdGenerator}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Result
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody,
  Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}
import v4.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.response.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockAuditService
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
    with MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory
    with MockIdGenerator {

  private val taxYear              = "2022-23"
  private val transactionReference = Some("transaction reference")
  private val mtdId: String        = "test-mtd-id"

  "CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController" should {
    "return an OK response" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
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

        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

        runErrorTest(RuleMisalignedPeriodError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    private val controller = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionValidatorFactory,
      service = mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakePutRequest(requestBodyJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAndAmendHistoricNonFhlPropertyBusinessAnnualSubmission",
        transactionName = "create-and-amend-historic-non-fhl-property-business-annual-submission",
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

    protected val requestBody: Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody =
      Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(None, None)

    protected val requestData: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

    protected val responseData: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse =
      CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(transactionReference)

    protected val validMtdJson: JsValue = Json.parse("""
     |{
     |   "annualAdjustments": {
     |      "lossBroughtForward": 100.00,
     |      "privateUseAdjustment": 200.00,
     |      "balancingCharge": 300.00,
     |      "businessPremisesRenovationAllowanceBalancingCharges": 400.00,
     |      "nonResidentLandlord": true,
     |      "rentARoom": {
     |         "jointlyLet": true
     |      }
     |   },
     |   "annualAllowances": {
     |      "annualInvestmentAllowance": 500.00,
     |      "zeroEmissionGoodsVehicleAllowance": 600.00,
     |      "businessPremisesRenovationAllowance": 700.00,
     |      "otherCapitalAllowance": 800.00,
     |      "costOfReplacingDomesticGoods": 900.00,
     |      "propertyIncomeAllowance": 1000.00
     |   }
     |}
     |""".stripMargin)

  }

}
