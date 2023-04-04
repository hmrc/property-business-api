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
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.{HistoricPropertyType, Nino}
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotSupportedError}
import api.models.hateoas.HateoasWrapper
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import v2.mocks.requestParsers.MockListHistoricUkPropertyPeriodSummariesRequestParser
import v2.mocks.services.MockListHistoricUkPropertyPeriodSummariesService
import v2.models.request.listHistoricUkPropertyPeriodSummaries._
import v2.models.response.listHistoricUkPropertyPeriodSummaries._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListHistoricUkPropertyPeriodSummariesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListHistoricUkPropertyPeriodSummariesService
    with MockListHistoricUkPropertyPeriodSummariesRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  "ListHistoricUkPropertyPeriodSummariesController" should {
    "return OK" when {
      "the valid request received is for a Fhl HistoricPropertyType" in new Test {
        lazy val propertyType: HistoricPropertyType = HistoricPropertyType.Fhl

        MockListHistoricUkPropertyPeriodSummariesRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockListHistoricUkPropertyPeriodSummariesService
          .listPeriodSummaries(requestData, propertyType)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrapList(responseData, hateoasData)
          .returns(responseDataWithHateoas)

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(Json.toJson(responseDataWithHateoas)))
      }

      "the valid request received is for a non-Fhl HistoricPropertyType" in new Test {
        lazy val propertyType: HistoricPropertyType = HistoricPropertyType.NonFhl

        MockListHistoricUkPropertyPeriodSummariesRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockListHistoricUkPropertyPeriodSummariesService
          .listPeriodSummaries(requestData, propertyType)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrapList(responseData, hateoasData)
          .returns(responseDataWithHateoas)

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(Json.toJson(responseDataWithHateoas)))
      }
    }

    "return an error as per spec" when {
      def parserErrorTest(property: HistoricPropertyType): Unit = {
        "the parser validation fails for a Fhl HistoricPropertyType" in new Test {
          lazy val propertyType: HistoricPropertyType = property

          MockListHistoricUkPropertyPeriodSummariesRequestParser
            .parseRequest(rawData)
            .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

          runErrorTest(NinoFormatError)
        }

        def serviceErrorTest(property: HistoricPropertyType): Unit = {
          "the service returns an error" in new Test {
            lazy val propertyType: HistoricPropertyType = property

            MockListHistoricUkPropertyPeriodSummariesRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockListHistoricUkPropertyPeriodSummariesService
              .listPeriodSummaries(requestData, propertyType)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

            runErrorTest(RuleTaxYearNotSupportedError)
          }

          Seq(HistoricPropertyType.Fhl, HistoricPropertyType.NonFhl)
            .foreach(propertyType => {
              parserErrorTest(propertyType)
              serviceErrorTest(propertyType)
            })
        }
      }
    }
  }

  trait Test extends ControllerTest {

    protected val propertyType: HistoricPropertyType

    private val controller = new ListHistoricUkPropertyPeriodSummariesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockListHistoricUkPropertyPeriodSummariesRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = propertyType match {
      case HistoricPropertyType.Fhl    => controller.handleFhlRequest(nino)(fakeGetRequest)
      case HistoricPropertyType.NonFhl => controller.handleNonFhlRequest(nino)(fakeGetRequest)
    }

    protected val rawData: ListHistoricUkPropertyPeriodSummariesRawData     = ListHistoricUkPropertyPeriodSummariesRawData(nino)
    protected val requestData: ListHistoricUkPropertyPeriodSummariesRequest = ListHistoricUkPropertyPeriodSummariesRequest(Nino(nino))

    protected val submissionPeriod: SubmissionPeriod = SubmissionPeriod("fromDate", "toDate")

    protected val responseData: ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod] =
      ListHistoricUkPropertyPeriodSummariesResponse(Seq(submissionPeriod))

    protected val hateoasData: ListHistoricUkPropertyPeriodSummariesHateoasData = ListHistoricUkPropertyPeriodSummariesHateoasData(nino, propertyType)

    protected val responseDataWithHateoas: HateoasWrapper[ListHistoricUkPropertyPeriodSummariesResponse[HateoasWrapper[SubmissionPeriod]]] =
      HateoasWrapper(ListHistoricUkPropertyPeriodSummariesResponse(Seq(HateoasWrapper(submissionPeriod, testHateoasLinks))), testHateoasLinks)

  }

}
