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

package v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission

import common.models.errors.RuleHistoricTaxYearNotSupportedError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.utils.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody,
  Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}
import v5.createAmendHistoricNonFhlUkPropertyAnnualSubmission.model.response.CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  private val nino: String    = "AA123456A"
  private val taxYear: String = "2019-20"

  "service" should {
    "service call successful" when {
      "return mapped non-fhl result" in new Test {
        MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None)))))

        await(service.amend(request)) shouldBe Right(
          ResponseWrapper(correlationId, CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionResponse(None)))
      }
    }
  }

  "unsuccessful" should {
    "map non fhl errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector
            .amend(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.amend(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = List(
        "INVALID_NINO"           -> NinoFormatError,
        "INVALID_TYPE"           -> InternalError,
        "INVALID_TAX_YEAR"       -> TaxYearFormatError,
        "INVALID_PAYLOAD"        -> InternalError,
        "INVALID_CORRELATIONID"  -> InternalError,
        "NOT_FOUND_PROPERTY"     -> NotFoundError,
        "NOT_FOUND"              -> NotFoundError,
        "GONE"                   -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleHistoricTaxYearNotSupportedError,
        "SERVER_ERROR"           -> InternalError,
        "SERVICE_UNAVAILABLE"    -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")
    implicit protected val correlationId: String          = "someCorrelationId"

    protected val service = new CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionService(
      connector = mockCreateAmendHistoricNonFhlUkPropertyAnnualSubmissionConnector
    )

    protected val requestBody: Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody =
      Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestBody(None, None)

    protected val request: CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData =
      Def1_CreateAmendHistoricNonFhlUkPropertyAnnualSubmissionRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

  }

}
