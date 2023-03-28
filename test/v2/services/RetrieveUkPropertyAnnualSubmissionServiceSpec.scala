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

package v2.services

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.RetrieveUkPropertyAnnualSubmissionConnector._
import api.controllers.EndpointLogContext
import v2.mocks.connectors.MockRetrieveUkPropertyAnnualSubmissionConnector
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import v2.models.request.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveUkPropertyAnnualSubmission.RetrieveUkPropertyAnnualSubmissionResponse
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukFhlProperty.UkFhlProperty
import v2.models.response.retrieveUkPropertyAnnualSubmission.ukNonFhlProperty.UkNonFhlProperty

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino: String                   = "AA123456A"
  val businessId: String             = "XAIS12345678910"
  val taxYear: TaxYear               = TaxYear.fromMtd("2020-21")
  implicit val correlationId: String = "X-123"

  val ukFhlProperty: UkFhlProperty       = UkFhlProperty(None, None)
  val ukNonFhlProperty: UkNonFhlProperty = UkNonFhlProperty(None, None)

  private val response =
    RetrieveUkPropertyAnnualSubmissionResponse("2020-01-01", Some(ukFhlProperty), Some(ukNonFhlProperty))

  private val request = RetrieveUkPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear)

  trait Test extends MockRetrieveUkPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveUkPropertyAnnualSubmissionService(
      connector = mockRetrieveUkPropertyConnector
    )
  }

  "service" when {
    "a uk result is found" should {
      "return a success result" in new Test {
        MockRetrieveUkPropertyConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, UkResult(response))))

        await(service.retrieveUkProperty(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "a non-uk result is found" should {
      "return a RULE_TYPE_OF_BUSINESS_INCORRECT error" in new Test {
        MockRetrieveUkPropertyConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, NonUkResult)))

        await(service.retrieveUkProperty(request)) shouldBe Left(ErrorWrapper(correlationId, RuleTypeOfBusinessIncorrectError))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(ifsErrorCode: String, error: MtdError): Unit =
          s"a $ifsErrorCode error is returned from the service" in new Test {

            MockRetrieveUkPropertyConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

            await(service.retrieveUkProperty(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = Seq(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_INCOMESOURCEID"    -> BusinessIdFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "INVALID_CORRELATIONID"     -> InternalError,
          "NO_DATA_FOUND"             -> NotFoundError,
          "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
          "SERVER_ERROR"              -> InternalError,
          "SERVICE_UNAVAILABLE"       -> InternalError
        )

        val extraTysErrors = Seq(
          "INVALID_INCOMESOURCE_ID" -> BusinessIdFormatError,
          "INVALID_CORRELATION_ID"  -> InternalError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
