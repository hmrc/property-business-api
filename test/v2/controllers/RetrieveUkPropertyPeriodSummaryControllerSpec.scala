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
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.hateoas.Method.GET
import api.models.outcomes.ResponseWrapper
import fixtures.RetrieveUkPropertyPeriodSummary.ResponseModelsFixture
import play.api.mvc.Result
import v2.mocks.requestParsers.MockRetrieveUkPropertyPeriodSummaryRequestParser
import v2.mocks.services.MockRetrieveUkPropertyPeriodSummaryService
import v2.models.request.retrieveUkPropertyPeriodSummary.{RetrieveUkPropertyPeriodSummaryRawData, RetrieveUkPropertyPeriodSummaryRequest}
import v2.models.response.retrieveUkPropertyPeriodSummary.{RetrieveUkPropertyPeriodSummaryHateoasData, RetrieveUkPropertyPeriodSummaryResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveUkPropertyPeriodSummaryService
    with MockRetrieveUkPropertyPeriodSummaryRequestParser
    with MockHateoasFactory
    with ResponseModelsFixture {

  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val taxYear      = "2022-23"

  "RetrieveUkPropertyPeriodSummaryController" should {
    "return (OK) 200 status" when {
      "the request received is valid" in new Test {
        MockRetrieveUkPropertyRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseData))))

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, Seq(testHateoasLink)))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseWithHateoas))
      }
    }

    "return validation error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveUkPropertyRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveUkPropertyRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest {

    private val controller = new RetrieveUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveUkPropertyRequestParser,
      service = mockRetrieveUkPropertyService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeGetRequest)

    protected val rawData: RetrieveUkPropertyPeriodSummaryRawData = RetrieveUkPropertyPeriodSummaryRawData(nino, businessId, taxYear, submissionId)

    protected val requestData: RetrieveUkPropertyPeriodSummaryRequest =
      RetrieveUkPropertyPeriodSummaryRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear), submissionId)

    protected val testHateoasLink: Link =
      Link(href = s"/individuals/business/property/$nino/$businessId/period/$taxYear/$submissionId", method = GET, rel = "self")

    protected val hateoasData: RetrieveUkPropertyPeriodSummaryHateoasData =
      RetrieveUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId)

    protected val responseData: RetrieveUkPropertyPeriodSummaryResponse = fullResponseModel
  }

}
