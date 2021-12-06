/*
 * Copyright 2021 HM Revenue & Customs
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
import v2.mocks.connectors.MockAmendForeignPropertyAnnualSubmissionConnector
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendForeignPropertyAnnualSubmission._
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignFhlEea._
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignNonFhl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2020-21"
  implicit val correlationId: String = "X-123"

  private val foreignFhlEea = ForeignFhlEea(None, None)

  private val foreignPropertyEntry = ForeignNonFhlEntry("FRA", None, None)

  val body: AmendForeignPropertyAnnualSubmissionRequestBody = AmendForeignPropertyAnnualSubmissionRequestBody(
    Some(foreignFhlEea),
    Some(Seq(foreignPropertyEntry))
  )

  private val request = AmendForeignPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear, body)

  trait Test extends MockAmendForeignPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendForeignPropertyAnnualSubmissionService(
      connector = mockAmendForeignPropertyAnnualSubmissionConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockAmendForeignPropertyAnnualSubmissionConnector.amendForeignProperty(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendForeignPropertyAnnualSubmission(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockAmendForeignPropertyAnnualSubmissionConnector.amendForeignProperty(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode(ifsErrorCode))))))

          await(service.amendForeignPropertyAnnualSubmission(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
        "INVALID_TAX_YEAR" -> TaxYearFormatError,
        "INCOMPATIBLE_PAYLOAD" -> RuleTypeOfBusinessIncorrectError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
        "BUSINESS_VALIDATION_FAILURE" -> RulePropertyIncomeAllowanceError,
        "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
        "MISSING_ALLOWANCES" -> DownstreamError,
        "INVALID_PAYLOAD" -> DownstreamError,
        "INVALID_CORRELATION_ID" -> DownstreamError,
        "DUPLICATE_COUNTRY_CODE" -> DownstreamError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
