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

package v4.propertyPeriodSummary.list

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.Json
import play.api.mvc.Result
import v4.propertyPeriodSummary.list.def1.model.response.SubmissionPeriod
import v4.propertyPeriodSummary.list.model.request.ListPropertyPeriodSummariesRequestData
import v4.propertyPeriodSummary.list.model.response.ListPropertyPeriodSummariesResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPropertyPeriodSummariesControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockListPropertyPeriodSummariesService
    with MockListPropertyPeriodSummariesValidatorFactory {

  private val businessId            = "XAIS12345678910"
  private val taxYear               = "2020-21"

  "ListPropertyPeriodSummariesController" should {
    "return Ok" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockListPropertyPeriodSummariesService
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

        MockListPropertyPeriodSummariesService
          .listPeriodSummaries(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)

      }
    }
  }

  trait Test extends ControllerTest {

    private val controller = new ListPropertyPeriodSummariesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockListPropertyPeriodSummariesValidatorFactory,
      service = mockService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeGetRequest)

    protected val requestData: ListPropertyPeriodSummariesRequestData =
      ListPropertyPeriodSummariesRequestData(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear))

    protected val responseData: ListPropertyPeriodSummariesResponse =
      ListPropertyPeriodSummariesResponse(List(SubmissionPeriod("someId", "fromDate", "toDate")))

  }

}
