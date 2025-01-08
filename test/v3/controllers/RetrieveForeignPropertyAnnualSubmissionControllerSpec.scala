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
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Result
import shared.hateoas.Method.GET
import shared.utils.MockIdGenerator
import v3.controllers.validators.MockRetrieveForeignPropertyAnnualSubmissionValidatorFactory
import v3.models.request.retrieveForeignPropertyAnnualSubmission._
import v3.models.response.retrieveForeignPropertyAnnualSubmission._
import v3.models.response.retrieveForeignPropertyAnnualSubmission.foreignFhlEea._
import v3.models.response.retrieveForeignPropertyAnnualSubmission.foreignProperty._
import v3.services.MockRetrieveForeignPropertyAnnualSubmissionService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveForeignPropertyAnnualSubmissionControllerSpec
    extends ControllerBaseSpec
    with MockAppConfig
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveForeignPropertyAnnualSubmissionService
    with MockRetrieveForeignPropertyAnnualSubmissionValidatorFactory
    with MockHateoasFactory
    with MockAuditService
    with MockIdGenerator {

  private val businessId          = "XAIS12345678910"
  private val taxYear             = "2020-21"
  val testHateoasLinks: Seq[Link] = List(Link(href = "/some/link", method = GET, rel = "someRel"))

  "RetrieveForeignPropertyAnnualSubmissionController" should {
    "return a successful response with status 200 (OK)" when {
      "the request received is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveForeignPropertyService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseBody))))

        MockHateoasFactory
          .wrap(responseBody, RetrieveForeignPropertyAnnualSubmissionHateoasData(validNino, businessId, taxYear))
          .returns(HateoasWrapper(responseBody, testHateoasLinks))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(Json.toJson(HateoasWrapper(responseBody, testHateoasLinks))))
      }
    }

    "return an error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "service errors occur" should {
        "the service returns an error" in new Test {
          willUseValidator(returningSuccess(requestData))

          MockRetrieveForeignPropertyService
            .retrieve(requestData)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, NotFoundError))))

          runErrorTest(NotFoundError)
        }
      }
    }
  }

  trait Test extends ControllerTest {

    protected val controller = new RetrieveForeignPropertyAnnualSubmissionController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveForeignPropertyAnnualSubmissionValidatorFactory,
      service = mockRetrieveForeignPropertyAnnualSubmissionService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.handleRequest(validNino, businessId, taxYear)(fakeRequest)

    protected val requestData: RetrieveForeignPropertyAnnualSubmissionRequestData =
      RetrieveForeignPropertyAnnualSubmissionRequestData(Nino(validNino), BusinessId(businessId), TaxYear.fromMtd(taxYear))

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
            List(
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
      Timestamp("2020-07-07T10:59:47.544Z"),
      foreignFhlEea = Some(foreignFhlEeaEntry),
      foreignNonFhlProperty = Some(List(foreignPropertyEntry))
    )

  }

}
