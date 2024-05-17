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

package v4.historicNonFhlUkPropertyPeriodSummary.list

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.Nino
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import api.models.outcomes.ResponseWrapper
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import mocks.MockIdGenerator
import play.api.libs.json.Json
import play.api.mvc.Result
import v4.historicNonFhlUkPropertyPeriodSummary.list.def1.model.response.SubmissionPeriod
import v4.historicNonFhlUkPropertyPeriodSummary.list.model.request.{Def1_ListHistoricNonFhlUkPropertyPeriodSummariesRequestData, ListHistoricNonFhlUkPropertyPeriodSummariesRequestData}
import v4.historicNonFhlUkPropertyPeriodSummary.list.model.response.{Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse, ListHistoricNonFhlUkPropertyPeriodSummariesResponse}

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

    private val controller = new ListHistoricNonFhlUkPropertyPeriodSummariesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockListHistoricNonFhlUkPropertyPeriodSummariesValidatorFactory,
      service = mockService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino)(fakeGetRequest)

    protected val requestData: ListHistoricNonFhlUkPropertyPeriodSummariesRequestData =
      Def1_ListHistoricNonFhlUkPropertyPeriodSummariesRequestData(Nino(nino))

    protected val submissionPeriod: SubmissionPeriod = SubmissionPeriod("fromDate", "toDate")

    protected val responseData: ListHistoricNonFhlUkPropertyPeriodSummariesResponse =
      Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse(List(submissionPeriod))

  }

}
