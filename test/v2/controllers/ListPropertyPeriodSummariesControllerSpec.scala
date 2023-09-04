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
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import v2.mocks.requestParsers.MockListPropertyPeriodSummariesRequestParser
import v2.mocks.services.MockListPropertyPeriodSummariesService
import v2.models.request.listPropertyPeriodSummaries.{ListPropertyPeriodSummariesRawData, ListPropertyPeriodSummariesRequest}
import v2.models.response.listPropertyPeriodSummaries.{ListPropertyPeriodSummariesHateoasData, ListPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPropertyPeriodSummariesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockListPropertyPeriodSummariesService
    with MockListPropertyPeriodSummariesRequestParser
    with MockHateoasFactory {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2020-21"

  "ListPropertyPeriodSummariesController" should {
    "return Ok" when {
      "the request received is valid" in new Test {
        MockListPropertyPeriodSummariesRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

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
        MockListPropertyPeriodSummariesRequestParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockListPropertyPeriodSummariesRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

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
      parser = mockListPropertyPeriodSummariesRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeGetRequest)

    protected val rawData: ListPropertyPeriodSummariesRawData = ListPropertyPeriodSummariesRawData(nino, businessId, taxYear)

    protected val requestData: ListPropertyPeriodSummariesRequest =
      ListPropertyPeriodSummariesRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear))

    protected val responseData: ListPropertyPeriodSummariesResponse =
      ListPropertyPeriodSummariesResponse(Seq(SubmissionPeriod("someId", "fromDate", "toDate")))

    protected val hateoasData: ListPropertyPeriodSummariesHateoasData = ListPropertyPeriodSummariesHateoasData(nino, businessId, taxYear)

    protected val responseDataWithHateoas: HateoasWrapper[ListPropertyPeriodSummariesResponse] = HateoasWrapper(responseData, testHateoasLinks)
  }

}
