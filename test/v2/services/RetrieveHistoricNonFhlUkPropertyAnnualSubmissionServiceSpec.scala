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
import api.support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.connectors.MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector
import v2.models.request.retrieveHistoricNonFhlUkPropertyAnnualSubmission.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  implicit private val correlationId: String = "X-123"
  private val nino                           = Nino("AA123456A")
  private val taxYear                        = TaxYear.fromMtd("2019-20")

  "retrieve" should {
    "return a success result" when {
      "a valid result is found" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        private val result = await(service.retrieve(request))
        result shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "return relevant mtd error according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector
            .retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          private val result = await(service.retrieve(request))
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

  trait Test extends MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new RetrieveHistoricNonFhlUkPropertyAnnualSubmissionService(
      connector = mockRetrieveHistoricNonFhlUkPropertyConnector
    )

    protected val response: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse =
      RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse(None, None)

    protected val request: RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData =
      RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequestData(nino, taxYear)

  }

}
