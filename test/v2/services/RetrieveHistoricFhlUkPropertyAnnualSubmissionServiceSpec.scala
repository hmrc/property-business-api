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

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockRetrieveHistoricFhlUkPropertyAnnualSubmissionConnector
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricFhlUkPropertyAnnualSubmission.RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveHistoricFhlUkPropertyAnnualSubmission.{
  AnnualAdjustments,
  AnnualAllowances,
  RentARoom,
  RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricFhlUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino: String                   = "AA123456A"
  val taxYear: String                = "2019-20"
  implicit val correlationId: String = "X-123"

  val annualAdjustments: AnnualAdjustments = AnnualAdjustments(
    Option(BigDecimal("100.11")),
    Option(BigDecimal("200.11")),
    Option(BigDecimal("105.11")),
    true,
    Option(BigDecimal("100.11")),
    false,
    Option(RentARoom(true))
  )

  val annualAllowances: AnnualAllowances = AnnualAllowances(
    Option(BigDecimal("100.11")),
    Option(BigDecimal("300.11")),
    Option(BigDecimal("405.11")),
    Option(BigDecimal("550.11"))
  )

  private val response =
    RetrieveHistoricFhlUkPropertyAnnualSubmissionResponse(Some(annualAdjustments), Some(annualAllowances))

  private val request = RetrieveHistoricFhlUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  trait Test extends MockRetrieveHistoricFhlUkPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveHistoricFhlUkPropertyAnnualSubmissionService(
      connector = mockRetrieveHistoricFhlUkPropertyConnector
    )
  }

  "service" when {
    "a valid result is found" should {
      "return a success result" in new Test {
        MockRetrieveHistoricFhlUkPropertyAnnualSubmissionConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        val result = await(service.retrieve(request))
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

            val result = await(service.retrieve(request))
            result shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
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
}
