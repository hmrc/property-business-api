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

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceOutcome
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.MockRetrieveHistoricFhlUkPropertyAnnualSubmissionConnector
import v2.models.request.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData
import v2.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  private val nino: String    = "AA123456A"
  private val taxYear: String = "2019-20"

  implicit private val correlationId: String = "X-123"

  "service" when {
    "a valid result is found" should {
      "return a success result" in new Test {
        MockRetrieveHistoricFhlUkPropertyAnnualSubmissionConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        val result: ServiceOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse] = await(service.retrieve(request))
        result shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveHistoricFhlUkPropertyAnnualSubmissionConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: ServiceOutcome[RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse] = await(service.retrieve(request))
            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = List(
          "INVALID_NINO"            -> NinoFormatError,
          "INVALID_TYPE"            -> InternalError,
          "INVALID_TAX_YEAR"        -> TaxYearFormatError,
          "INVALID_CORRELATIONID"   -> InternalError,
          "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
          "NOT_FOUND_PERIOD"        -> NotFoundError,
          "TAX_YEAR_NOT_SUPPORTED"  -> RuleHistoricTaxYearNotSupportedError,
          "SERVER_ERROR"            -> InternalError,
          "SERVICE_UNAVAILABLE"     -> InternalError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockRetrieveHistoricFhlUkPropertyAnnualSubmissionConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new RetrieveHistoricFhlUkPropertyAnnualSubmissionService(
      connector = mockRetrieveHistoricFhlUkPropertyConnector
    )

    protected val response: RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse =
      RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse(None, None)

    protected val request: RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData =
      RetrieveHistoricFhlUkPropertyAnnualSubmissionRequestData(Nino(nino), TaxYear.fromMtd(taxYear))

  }

}
