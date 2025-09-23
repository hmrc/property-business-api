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

package v6.createAmendForeignPropertyCumulativePeriodSummary

import common.models.errors.RuleMisalignedPeriodError
import play.api.Configuration
import play.api.libs.json.JsObject
import play.api.mvc.Result
import play.api.test.FakeRequest
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.model.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures
import v6.createAmendForeignPropertyCumulativePeriodSummary.def1.model.request.Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData
import v6.createAmendForeignPropertyCumulativePeriodSummary.model.request.CreateAmendForeignPropertyCumulativePeriodSummaryRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendForeignPropertyCumulativePeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateAmendForeignPropertyCumulativePeriodSummaryService
    with MockCreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory
    with MockIdGenerator
    with Def1_CreateAmendForeignPropertyCumulativePeriodSummaryFixtures {

  private val taxYear                            = "2025-26"
  private val businessId                         = "XAIS12345678910"
  def fakePutRequest[T](body: T): FakeRequest[T] = fakeRequest.withBody(body)

  "CreateAmendForeignPropertyCumulativePeriodSummaryController" should {
    "return a successful response with status 204 (NO_CONTENT)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedCreateAmendForeignPropertyCumulativePeriodSummaryService
          .createAmendForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = NO_CONTENT, maybeExpectedResponseBody = None)

      }

      "return the error as per spec" when {
        "the parser validation fails" in new Test {
          willUseValidator(returning(NinoFormatError))

          runErrorTest(NinoFormatError)
        }

        "the service returns an error" in new Test {
          willUseValidator(returningSuccess(requestData))

          MockedCreateAmendForeignPropertyCumulativePeriodSummaryService
            .createAmendForeignProperty(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleMisalignedPeriodError))))

          runErrorTest(RuleMisalignedPeriodError)
        }
      }
    }
  }

  trait Test extends ControllerTest {

    val controller: CreateAmendForeignPropertyCumulativePeriodSummaryController = new CreateAmendForeignPropertyCumulativePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      service = mockCreateAmendForeignPropertyCumulativePeriodSummaryService,
      auditService = mockAuditService,
      validatorFactory = mockCreateAmendForeignPropertyCumulativePeriodSummaryValidatorFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear)(fakePutRequest(requestBody))

    val requestBody: JsObject = JsObject.empty

    protected val requestData: CreateAmendForeignPropertyCumulativePeriodSummaryRequestData =
      Def1_CreateAmendForeignPropertyCumulativePeriodSummaryRequestData(
        nino = Nino(validNino),
        businessId = BusinessId(businessId),
        taxYear = TaxYear.fromMtd(taxYear),
        body = regularExpensesRequestBody)

  }

}
