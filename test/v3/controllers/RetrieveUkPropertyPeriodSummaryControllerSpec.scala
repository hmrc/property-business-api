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

import common.models.domain.SubmissionId
import config.MockAppConfig
import fixtures.RetrieveUkPropertyPeriodSummary.ResponseModelsFixture
import play.api.Configuration
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method.GET
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.domain.{BusinessId, Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v3.controllers.validators.MockRetrieveUkPropertyPeriodSummaryValidatorFactory
import v3.models.request.retrieveUkPropertyPeriodSummary.RetrieveUkPropertyPeriodSummaryRequestData
import v3.models.response.retrieveUkPropertyPeriodSummary.{RetrieveUkPropertyPeriodSummaryHateoasData, RetrieveUkPropertyPeriodSummaryResponse}
import v3.services.MockRetrieveUkPropertyPeriodSummaryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkPropertyPeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockRetrieveUkPropertyPeriodSummaryService
    with MockRetrieveUkPropertyPeriodSummaryValidatorFactory
    with MockHateoasFactory
    with ResponseModelsFixture {

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

        MockHateoasFactory
          .wrap(responseData, hateoasData)
          .returns(HateoasWrapper(responseData, List(testHateoasLink)))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseWithHateoas))
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
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear, submissionId)(fakeGetRequest)

    protected val requestData: RetrieveUkPropertyPeriodSummaryRequestData =
      RetrieveUkPropertyPeriodSummaryRequestData(Nino(validNino), BusinessId(businessId), TaxYear.fromMtd(taxYear), SubmissionId(submissionId))

    protected val testHateoasLink: Link =
      Link(href = s"/individuals/business/property/$validNino/$businessId/period/$taxYear/$submissionId", method = GET, rel = "self")

    protected val hateoasData: RetrieveUkPropertyPeriodSummaryHateoasData =
      RetrieveUkPropertyPeriodSummaryHateoasData(validNino, businessId, taxYear, submissionId)

    protected val responseData: RetrieveUkPropertyPeriodSummaryResponse = fullResponseModel
  }

}
