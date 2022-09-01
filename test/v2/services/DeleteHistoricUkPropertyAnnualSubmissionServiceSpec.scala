/*
 * Copyright 2022 HM Revenue & Customs
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

import uk.gov.hmrc.http.HeaderCarrier
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockDeleteHistoricUkPropertyAnnualSubmissionConnector
import v2.models.domain.{ HistoricPropertyType, Nino, TaxYear }
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequest

import scala.concurrent.Future

class DeleteHistoricUkPropertyAnnualSubmissionServiceSpec extends ServiceSpec {

  val nino: String                       = "AA123456A"
  val mtdTaxYear: String                 = "2021-22"
  val taxYear: TaxYear                   = TaxYear.fromMtd(mtdTaxYear)
  val propertyType: HistoricPropertyType = HistoricPropertyType.Fhl

  implicit val correlationId: String = "X-123"

  private val requestData = DeleteHistoricUkPropertyAnnualSubmissionRequest(Nino(nino), taxYear, propertyType)

  trait Test extends MockDeleteHistoricUkPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new DeleteHistoricUkPropertyAnnualSubmissionService(
      connector = mockDeleteHistoricUkPropertyAnnualSubmissionConnector
    )
  }

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

        val input = Seq(
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
}
