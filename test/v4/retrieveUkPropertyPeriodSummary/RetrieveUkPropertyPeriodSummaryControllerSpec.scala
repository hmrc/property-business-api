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

package v4.retrieveUkPropertyPeriodSummary

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.{BusinessId, Nino, SubmissionId, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import config.MockAppConfig
import play.api.Configuration
import play.api.mvc.Result
import v4.retrieveUkPropertyPeriodSummary.def1.model.Def1_RetrieveUkPropertyPeriodSummaryFixture
import v4.retrieveUkPropertyPeriodSummary.model.request._
import v4.retrieveUkPropertyPeriodSummary.model.response.RetrieveUkPropertyPeriodSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockRetrieveUkPropertyPeriodSummaryService
    with MockRetrieveUkPropertyPeriodSummaryValidatorFactory
    with Def1_RetrieveUkPropertyPeriodSummaryFixture {

  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val taxYear      = "2022-23"

  "RetrieveUkPropertyPeriodSummaryController" should {
    "return (OK) 200 status" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(fullMtdJson))
      }
    }

    "return validation error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller = new RetrieveUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveUkPropertyPeriodSummaryValidatorFactory,
      service = mockRetrieveUkPropertyService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeGetRequest)

    protected val requestData: RetrieveUkPropertyPeriodSummaryRequestData =
      Def1_RetrieveUkPropertyPeriodSummaryRequestData(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear), SubmissionId(submissionId))

    protected val responseData: RetrieveUkPropertyPeriodSummaryResponse = fullResponse
  }

}
