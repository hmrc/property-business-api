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
import api.models.domain.{BusinessId, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.Json
import play.api.mvc.Result
import v2.controllers.validators.MockListPropertyPeriodSummariesValidatorFactory
import v2.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRequestData
import v2.models.response.listPropertyPeriodSummaries.{ListPropertyPeriodSummariesHateoasData, ListPropertyPeriodSummariesResponse, SubmissionPeriod}
import v2.services.MockListPropertyPeriodSummariesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPropertyPeriodSummariesControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockListPropertyPeriodSummariesService
    with MockListPropertyPeriodSummariesValidatorFactory
    with MockHateoasFactory {

  private val businessId            = "XAIS12345678910"
  private val taxYear               = "2020-21"


  "ListPropertyPeriodSummariesController" should {
    "return Ok" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockListPropertyPeriodSummariesService
          .listPeriodSummaries(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(responseDataWithHateoas)

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(Json.toJson(responseDataWithHateoas)))
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
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeGetRequest)

    protected val requestData: ListPropertyPeriodSummariesRequestData =
      ListPropertyPeriodSummariesRequestData(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear))

    protected val responseData: ListPropertyPeriodSummariesResponse =
      ListPropertyPeriodSummariesResponse(List(SubmissionPeriod("someId", "fromDate", "toDate")))

    protected val hateoasData: ListPropertyPeriodSummariesHateoasData = ListPropertyPeriodSummariesHateoasData(nino, businessId, taxYear)

    protected val responseDataWithHateoas: HateoasWrapper[ListPropertyPeriodSummariesResponse] = HateoasWrapper(responseData, testHateoasLinks)
  }

}
