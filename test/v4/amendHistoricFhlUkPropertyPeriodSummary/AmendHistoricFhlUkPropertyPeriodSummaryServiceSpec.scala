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

package v4.amendHistoricFhlUkPropertyPeriodSummary

import api.controllers.EndpointLogContext
import api.models.domain.{Nino, PeriodId}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v4.amendHistoricFhlUkPropertyPeriodSummary.model.request.{Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody, Def1_AmendHistoricFhlUkPiePeriodSummaryRequestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendHistoricFhlUkPropertyPeriodSummaryServiceSpec extends UnitSpec {

  private val nino: String    = "AA123456A"
  private val periodId = PeriodId(from = "2017-04-06", to = "2017-07-04")

  implicit private val correlationId: String = "X-123"

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockAmendHistoricFhlUkPropertyPeriodSummaryConnector
          .amend(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amend(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockAmendHistoricFhlUkPropertyPeriodSummaryConnector
            .amend(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.amend(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }


      val input = List(
        "INVALID_NINO"                -> NinoFormatError,
        "INVALID_TYPE"                -> InternalError,
        "INVALID_PAYLOAD"             -> InternalError,
        "INVALID_DATE_FROM"           -> PeriodIdFormatError,
        "INVALID_DATE_TO"             -> PeriodIdFormatError,
        "INVALID_CORRELATIONID"       -> InternalError,
        "SUBMISSION_PERIOD_NOT_FOUND" -> NotFoundError,
        "NOT_FOUND_PROPERTY"          -> NotFoundError,
        "NOT_FOUND_INCOME_SOURCE"     -> NotFoundError,
        "NOT_FOUND"                   -> NotFoundError,
        "BOTH_EXPENSES_SUPPLIED"      -> RuleBothExpensesSuppliedError,
        "SERVER_ERROR"                -> InternalError,
        "SERVICE_UNAVAILABLE"         -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockAmendHistoricFhlUkPropertyPeriodSummaryConnector {
    implicit protected val hc: HeaderCarrier              = HeaderCarrier()
    implicit protected val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    protected val service = new AmendHistoricFhlUkPropertyPeriodSummaryService(
      connector = mockAmendHistoricFhlUkPropertyPeriodSummaryConnector
    )

    private val body: Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody =
      Def1_AmendHistoricFhlUkPiePeriodSummaryRequestBody(None, None)

    protected val request: Def1_AmendHistoricFhlUkPiePeriodSummaryRequestData =
      Def1_AmendHistoricFhlUkPiePeriodSummaryRequestData(Nino(nino), periodId, body)

  }

}
