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

import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import shared.hateoas.Method.GET
import v3.controllers.validators.MockListPropertyPeriodSummariesValidatorFactory
import v3.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRequestData
import v3.models.response.listPropertyPeriodSummaries.{ListPropertyPeriodSummariesHateoasData, ListPropertyPeriodSummariesResponse, SubmissionPeriod}
import v3.services.MockListPropertyPeriodSummariesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPropertyPeriodSummariesControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockListPropertyPeriodSummariesService
    with MockListPropertyPeriodSummariesValidatorFactory
    with MockHateoasFactory {

  private val businessId          = "XAIS12345678910"
  private val taxYear             = "2020-21"
  val testHateoasLinks: Seq[Link] = List(Link(href = "/some/link", method = GET, rel = "someRel"))

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

    protected val controller = new ListPropertyPeriodSummariesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockListPropertyPeriodSummariesValidatorFactory,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear)(fakeGetRequest)

    protected val requestData: ListPropertyPeriodSummariesRequestData =
      ListPropertyPeriodSummariesRequestData(Nino(validNino), BusinessId(businessId), TaxYear.fromMtd(taxYear))

    protected val responseData: ListPropertyPeriodSummariesResponse =
      ListPropertyPeriodSummariesResponse(List(SubmissionPeriod("someId", "fromDate", "toDate")))

    protected val hateoasData: ListPropertyPeriodSummariesHateoasData = ListPropertyPeriodSummariesHateoasData(validNino, businessId, taxYear)

    protected val responseDataWithHateoas: HateoasWrapper[ListPropertyPeriodSummariesResponse] = HateoasWrapper(responseData, testHateoasLinks)
  }

}
