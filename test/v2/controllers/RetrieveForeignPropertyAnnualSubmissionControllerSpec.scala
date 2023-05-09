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
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.HateoasWrapper
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import v2.mocks.requestParsers.MockRetrieveForeignPropertyAnnualSubmissionRequestParser
import v2.mocks.services.MockRetrieveForeignPropertyAnnualSubmissionService
import v2.models.request.retrieveForeignPropertyAnnualSubmission._
import v2.models.response.retrieveForeignPropertyAnnualSubmission._
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea._
import v2.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveForeignPropertyAnnualSubmissionService
    with MockRetrieveForeignPropertyAnnualSubmissionRequestParser
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val businessId = "XAIS12345678910"
  private val taxYear    = "2020-21"

  "RetrieveForeignPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {

        MockRetrieveForeignPropertyRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveForeignPropertyAnnualSubmissionHateoasData(nino, businessId, taxYear))
          .returns(HateoasWrapper(responseBody, testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(Json.toJson(HateoasWrapper(responseBody, testHateoasLinks))))
      }
    }

    "return an error as per spec" when {
      "the parser validation fails" in new Test {

        MockRetrieveForeignPropertyRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, BadRequestError, None)))

        runErrorTest(BadRequestError)
      }

      "service errors occur" should {
        "the service returns an error" in new Test {

          MockRetrieveForeignPropertyRequestParser
            .parse(rawData)
            .returns(Right(requestData))

          MockRetrieveForeignPropertyService
            .retrieve(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

          runErrorTest(NotFoundError)
        }
      }
    }
  }

  trait Test extends ControllerTest {

    private val controller = new RetrieveForeignPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRetrieveForeignPropertyAnnualSubmissionRequestParser,
      service = mockRetrieveForeignPropertyAnnualSubmissionService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, businessId, taxYear)(fakeRequest)

    protected val rawData: RetrieveForeignPropertyAnnualSubmissionRawData = RetrieveForeignPropertyAnnualSubmissionRawData(nino, businessId, taxYear)

    protected val requestData: RetrieveForeignPropertyAnnualSubmissionRequest =
      RetrieveForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, TaxYear.fromMtd(taxYear))

    protected val foreignFhlEeaEntry: ForeignFhlEeaEntry = ForeignFhlEeaEntry(
      Some(
        ForeignFhlEeaAdjustments(
          Some(5000.99),
          Some(5000.99),
          Some(true)
        )),
      Some(
        ForeignFhlEeaAllowances(
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99),
          Some(5000.99)
        ))
    )

    protected val foreignPropertyEntry: ForeignPropertyEntry = ForeignPropertyEntry(
      "FRA",
      Some(
        ForeignPropertyAdjustments(
          Some(5000.99),
          Some(5000.99)
        )),
      Some(
        ForeignPropertyAllowances(
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(100.25),
          Some(
            Seq(
              StructuredBuildingAllowance(
                3545.12,
                Some(FirstYear(
                  "2020-03-29",
                  3453.34
                )),
                Building(
                  Some("Building Name"),
                  Some("12"),
                  "TF3 4GH"
                )
              )))
        ))
    )

    protected val responseBody: RetrieveForeignPropertyAnnualSubmissionResponse = RetrieveForeignPropertyAnnualSubmissionResponse(
      "2020-07-07T10:59:47.544Z",
      foreignFhlEea = Some(foreignFhlEeaEntry),
      foreignNonFhlProperty = Some(Seq(foreignPropertyEntry))
    )

  }

}
