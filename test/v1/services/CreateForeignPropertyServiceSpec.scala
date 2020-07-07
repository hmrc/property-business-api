/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockCreateForeignPropertyConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.createForeignProperty.{CreateForeignPropertyRequestBody, CreateForeignPropertyRequestData, ForeignFhlEea, ForeignFhlEeaExpenditure, ForeignFhlEeaIncome, ForeignProperty, ForeignPropertyExpenditure, ForeignPropertyIncome, RentIncome}
import v1.models.response.create.CreateForeignPropertyResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateForeignPropertyServiceSpec extends UnitSpec {

  val taxYear = "2018-04-06"
  val nino = Nino("AA123456A")
  private val correlationId = "X-123"

  val body = CreateForeignPropertyRequestBody(
    "2020-01-01",
    "2020-01-31",
    Some(ForeignFhlEea(
      ForeignFhlEeaIncome(5000.99, Some(5000.99)),
      Some(ForeignFhlEeaExpenditure(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ))
    )),
    Some(Seq(ForeignProperty("FRA",
      ForeignPropertyIncome(
        RentIncome(5000.99, 5000.99),
        false,
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ),
      Some(ForeignPropertyExpenditure(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99)
      ))))
    ))

  val response = CreateForeignPropertyResponse("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  private val requestData = CreateForeignPropertyRequestData(nino, taxYear, body)

  trait Test extends MockCreateForeignPropertyConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new CreateForeignPropertyService(
      connector = mockCreateForeignPropertyConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockCreateForeignPropertyConnector.amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.createForeignProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockCreateForeignPropertyConnector.amend(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.createForeignProperty(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "FORMAT_BUSINESS_ID" -> BusinessIdFormatError,
        "RULE_OVERLAPPING_PERIOD" -> RuleOverlappingPeriodError,
        "RULE_MISALIGNED_PERIOD" -> RuleMisalignedPeriodError,
        "RULE_NOT_CONTIGUOUS_PERIOD" -> RuleNotContiguousPeriodError,
        "NOT_FOUND" -> NotFoundError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
