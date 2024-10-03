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

package v3.services

import api.controllers.EndpointLogContext
import api.models.domain.{HistoricPropertyType, Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import uk.gov.hmrc.http.HeaderCarrier
import v3.connectors.MockDeleteHistoricUkPropertyAnnualSubmissionConnector
import v3.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequestData

import scala.concurrent.Future

class DeleteHistoricUkPropertyAnnualSubmissionServiceSpec extends ServiceSpec {

  implicit private val correlationId: String = "X-123"
  private val nino                           = Nino("AA123456A")
  private val taxYear                        = TaxYear.fromMtd("2021-22")
  private val propertyType                   = HistoricPropertyType.Fhl

  "service" when {
    "service call successful" should {
      "return mapped result" in new Test {
        MockDeleteHistoricUkPropertyAnnualSubmissionConnector
          .deleteHistoricUkPropertyAnnualSubmission(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.deleteHistoricUkPropertyAnnualSubmission(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "unsuccessful" should {
      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockDeleteHistoricUkPropertyAnnualSubmissionConnector
              .deleteHistoricUkPropertyAnnualSubmission(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.deleteHistoricUkPropertyAnnualSubmission(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = List(
          "INVALID_NINO"           -> NinoFormatError,
          "INVALID_TAX_YEAR"       -> TaxYearFormatError,
          "INVALID_TYPE"           -> InternalError,
          "INVALID_PAYLOAD"        -> InternalError,
          "INVALID_CORRELATIONID"  -> InternalError,
          "NOT_FOUND"              -> NotFoundError,
          "NOT_FOUND_PROPERTY"     -> NotFoundError,
          "GONE"                   -> NotFoundError,
          "TAX_YEAR_NOT_SUPPORTED" -> RuleHistoricTaxYearNotSupportedError,
          "SERVER_ERROR"           -> InternalError,
          "SERVICE_UNAVAILABLE"    -> InternalError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockDeleteHistoricUkPropertyAnnualSubmissionConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new DeleteHistoricUkPropertyAnnualSubmissionService(
      connector = mockDeleteHistoricUkPropertyAnnualSubmissionConnector
    )

    protected val requestData: DeleteHistoricUkPropertyAnnualSubmissionRequestData =
      DeleteHistoricUkPropertyAnnualSubmissionRequestData(nino, taxYear, propertyType)

  }

}
