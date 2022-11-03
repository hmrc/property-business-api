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
import v2.mocks.connectors.MockListPropertyPeriodSummariesConnector
import v2.models.domain.{Nino, TaxYear}
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.listPropertyPeriodSummaries.ListPropertyPeriodSummariesRequest
import v2.models.response.listPropertyPeriodSummaries.{ListPropertyPeriodSummariesResponse, SubmissionPeriod}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPropertyPeriodSummariesServiceSpec extends UnitSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: TaxYear = TaxYear.fromMtd("2020-21")
  implicit val correlationId: String = "X-123"

  private val request = ListPropertyPeriodSummariesRequest(Nino(nino), businessId, taxYear)

  private val response = ListPropertyPeriodSummariesResponse(Seq(
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22"),
    SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3d", "2020-08-22", "2020-08-22")
  ))

  trait Test extends MockListPropertyPeriodSummariesConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new ListPropertyPeriodSummariesService(
      connector = mockListPropertyPeriodSummariesConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockListPropertyPeriodSummariesConnector.listPeriodSummaries(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.listPeriodSummaries(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockListPropertyPeriodSummariesConnector.listPeriodSummaries(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(ifsErrorCode))))))

          await(service.listPeriodSummaries(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
        "INVALID_TAX_YEAR" -> TaxYearFormatError,
        "INVALID_CORRELATIONID" -> InternalError,
        "NO_DATA_FOUND" -> NotFoundError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
        "SERVER_ERROR" -> InternalError,
        "SERVICE_UNAVAILABLE" -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
