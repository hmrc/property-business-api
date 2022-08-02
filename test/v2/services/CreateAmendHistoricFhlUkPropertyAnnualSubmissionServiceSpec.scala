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
import v2.mocks.connectors.MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.createAmendHistoricFhlUkPropertyAnnualSubmission.{
  CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest,
  CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody,
  HistoricFhlAnnualAdjustments,
  HistoricFhlAnnualAllowances
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendHistoricFhlUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino: String                   = "AA123456A"
  val taxYear: String                = "2022-23"
  implicit val correlationId: String = "X-123"

  private val annualAdjustments = HistoricFhlAnnualAdjustments(
    Some(BigDecimal("105.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("120.11")),
    true,
    Some(BigDecimal("101.11")),
    false,
    Some(UkPropertyAdjustmentsRentARoom(true))
  )

  private val annualAllowances = HistoricFhlAnnualAllowances(
    Some(BigDecimal("100.11")),
    Some(BigDecimal("200.11")),
    Some(BigDecimal("425.11")),
    Some(BigDecimal("550.11"))
  )

  val body: CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequestBody(
    Some(annualAdjustments),
    Some(annualAllowances)
  )

  private val request = CreateAmendHistoricFhlUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear), body)

  trait Test extends MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateAmendHistoricFhlUkPropertyAnnualSubmissionService(
      connector = mockCreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amend(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockCreateAmendHistoricFhlUkPropertyAnnualSubmissionConnector
            .amend(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          await(service.amend(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_NINO"        -> NinoFormatError,
        "INVALID_TYPE"        -> InternalError,
        "INVALID_TAX_YEAR"    -> TaxYearFormatError,
        "INVALID_PAYLOAD"     -> InternalError,
        "NOT_FOUND_PROPERTY"  -> NotFoundError,
        "GONE"                -> InternalError,
        "SERVER_ERROR"        -> InternalError,
        "SERVICE_UNAVAILABLE" -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}