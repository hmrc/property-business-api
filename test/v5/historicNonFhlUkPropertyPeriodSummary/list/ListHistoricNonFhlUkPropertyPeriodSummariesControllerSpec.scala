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

package v5.historicNonFhlUkPropertyPeriodSummary.list

import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.Nino
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v5.historicNonFhlUkPropertyPeriodSummary.list.def1.model.response.SubmissionPeriod
import v5.historicNonFhlUkPropertyPeriodSummary.list.model.request.{
  Def1_ListHistoricNonFhlUkPropertyPeriodSummariesRequestData,
  ListHistoricNonFhlUkPropertyPeriodSummariesRequestData
}
import v5.historicNonFhlUkPropertyPeriodSummary.list.model.response.{
  Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse,
  ListHistoricNonFhlUkPropertyPeriodSummariesResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListHistoricNonFhlUkPropertyPeriodSummariesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListHistoricNonFhlUkPropertyPeriodSummariesService
    with MockListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory
    with MockAuditService
    with MockIdGenerator {

  "ListHistoricUkPropertyPeriodSummariesController" should {
    "return OK" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedListHistoricNonFhlUkPropertyPeriodSummariesService
          .listPeriodSummaries(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(Json.toJson(responseData)))
      }
    }

    "return an error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedListHistoricNonFhlUkPropertyPeriodSummariesService
          .listPeriodSummaries(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller = new ListHistoricNonFhlUkPropertyPeriodSummariesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory,
      service = mockService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino)(fakeGetRequest)

    protected val requestData: ListHistoricNonFhlUkPropertyPeriodSummariesRequestData =
      Def1_ListHistoricNonFhlUkPropertyPeriodSummariesRequestData(Nino(validNino))

    protected val submissionPeriod: SubmissionPeriod = SubmissionPeriod("fromDate", "toDate")

    protected val responseData: ListHistoricNonFhlUkPropertyPeriodSummariesResponse =
      Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse(List(submissionPeriod))

  }

}
