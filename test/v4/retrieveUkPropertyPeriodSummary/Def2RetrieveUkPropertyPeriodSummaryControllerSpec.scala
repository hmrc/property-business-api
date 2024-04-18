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
import api.hateoas.Method.GET
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.domain.{BusinessId, Nino, SubmissionId, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v4.retrieveUkPropertyPeriodSummary.def2.model.{Def2_RetrieveUkPropertyPeriodSummaryConsolidatedFixture, Def2_RetrieveUkPropertyPeriodSummaryFixture}
import v4.retrieveUkPropertyPeriodSummary.model.request._
import v4.retrieveUkPropertyPeriodSummary.model.response.{RetrieveUkPropertyPeriodSummaryHateoasData, RetrieveUkPropertyPeriodSummaryResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Def2RetrieveUkPropertyPeriodSummaryControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveUkPropertyPeriodSummaryService
    with MockRetrieveUkPropertyPeriodSummaryValidatorFactory
    with MockHateoasFactory
    with Def2_RetrieveUkPropertyPeriodSummaryFixture {

  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val taxYear      = "2024-25"

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

    private val controller = new RetrieveUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveUkPropertyPeriodSummaryValidatorFactory,
      service = mockRetrieveUkPropertyService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeGetRequest)

    protected val requestData: RetrieveUkPropertyPeriodSummaryRequestData =
      Def2_RetrieveUkPropertyPeriodSummaryRequestData(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear), SubmissionId(submissionId))

    protected val testHateoasLink: Link =
      Link(href = s"/individuals/business/property/$nino/$businessId/period/$taxYear/$submissionId", method = GET, rel = "self")

    protected val hateoasData: RetrieveUkPropertyPeriodSummaryHateoasData =
      RetrieveUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId)

    protected val responseData: RetrieveUkPropertyPeriodSummaryResponse = fullResponseModel
  }

}

class Def2RetrieveUkPropertyPeriodSummaryConsolidatedControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveUkPropertyPeriodSummaryService
    with MockRetrieveUkPropertyPeriodSummaryValidatorFactory
    with MockHateoasFactory
    with Def2_RetrieveUkPropertyPeriodSummaryConsolidatedFixture {

  private val businessId   = "XAIS12345678910"
  private val submissionId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val taxYear      = "2024-25"

  "RetrieveUkPropertyPeriodSummaryController" should {
    "return (OK) 200 status" when {
      "the request received is valid" in new ConsolidatedTest {
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
      "the parser validation fails" in new ConsolidatedTest {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new ConsolidatedTest {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveUkPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait ConsolidatedTest extends ControllerTest {

    private val controller = new RetrieveUkPropertyPeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveUkPropertyPeriodSummaryValidatorFactory,
      service = mockRetrieveUkPropertyService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear, submissionId)(fakeGetRequest)

    protected val requestData: RetrieveUkPropertyPeriodSummaryRequestData =
      Def2_RetrieveUkPropertyPeriodSummaryConsolidatedRequestData(Nino(nino), BusinessId(businessId), TaxYear.fromMtd(taxYear), SubmissionId(submissionId))

    protected val testHateoasLink: Link =
      Link(href = s"/individuals/business/property/$nino/$businessId/period/$taxYear/$submissionId", method = GET, rel = "self")

    protected val hateoasData: RetrieveUkPropertyPeriodSummaryHateoasData =
      RetrieveUkPropertyPeriodSummaryHateoasData(nino, businessId, taxYear, submissionId)

    protected val responseData: RetrieveUkPropertyPeriodSummaryResponse = fullResponseModel
  }
}
