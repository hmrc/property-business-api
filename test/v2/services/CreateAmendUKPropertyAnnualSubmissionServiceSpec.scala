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
import v2.models.request.amendForeignPropertyAnnualSubmission.foreignProperty._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateAmendUKPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2022-23"
  implicit val correlationId: String = "X-123"

  private val ukFhlProperty = ukFhlProperty(
    Some(ukFhlPropertyAdjustments(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      true,
      Some(rentARoom(
        true
      ))
    )),
    Some(ukFhlPropertyAllowances(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99)
    ))
  )

  private val ukNonFhlProperty = ukNonFhlProperty(
    Some(ukNonFhlPropertyAdjustments(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      true,
      Some(rentARoom(
        true
      ))
    )),
    Some(ukNonFhlPropertyAllowances(
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(5000.99),
      Some(Seq(structuredBuildingAllowance(
        5000.99,
        Some(firstYear(
          "2020-01-01",
          5000.99
        )),
        Some(building(
          Some("Green Oak's"),
          Some("16AD"),
          Some("GF49JH")
        ))
      )))
    ))
  )

  val body: AmendUKPropertyAnnualSubmissionRequestBody = AmendUKPropertyAnnualSubmissionRequestBody(
    Some(foreignFhlEea),
    Some(Seq(foreignPropertyEntry))
  )

  private val request = AmendUKPropertyAnnualSubmissionRequest(Nino(nino), businessId, taxYear, body)

  trait Test extends MockAmendUKPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendUKPropertyAnnualSubmissionService(
      connector = mockAmendUKPropertyAnnualSubmissionConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockAmendUKPropertyAnnualSubmissionConnector.amendUKProperty(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendUKPropertyAnnualSubmission(request)) shouldBe Right(ResponseWrapper(correlationId, ()))
    }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockAmendUKPropertyAnnualSubmissionConnector.amendUKProperty(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode(ifsErrorCode))))))

          await(service.amendUKPropertyAnnualSubmission(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR" -> TaxYearFormatError,
        "INVALID_INCOME_SOURCE_ID" -> BusinessIdFormatError,
        "INVALID_PAYLOAD" -> DownstreamError,
        "INVALID_CORRELATION_ID" -> DownstreamError,
        "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
        "INCOMPATIBLE_PAYLOAD" -> RuleTypeOfBusinessIncorrect,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
        "DUPLICATE_COUNTRY_CODE" -> DownstreamError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}