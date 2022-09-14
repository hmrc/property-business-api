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
import v2.mocks.connectors.MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.retrieveHistoricNonFhlUkPropertyAnnualSubmission.RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest
import v2.models.response.retrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse.{
  AnnualAdjustments,
  AnnualAllowances,
  RentARoom,
  RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveHistoricNonFhlUkPropertyAnnualSubmissionServiceSpec extends UnitSpec {

  val nino: String                   = "AA123456A"
  val taxYear: String                = "2019-20"
  implicit val correlationId: String = "X-123"

  val annualAdjustments: AnnualAdjustments = AnnualAdjustments(
    lossBroughtForward = Option(BigDecimal("100.11")),
    privateUseAdjustment = Option(BigDecimal("200.11")),
    balancingCharge = Option(BigDecimal("105.11")),
    businessPremisesRenovationAllowanceBalancingCharges = Option(BigDecimal("100.11")),
    nonResidentLandlord = false,
    rentARoom = Option(RentARoom(true))
  )

  val annualAllowances: AnnualAllowances = AnnualAllowances(
    annualInvestmentAllowance = Option(BigDecimal("100.11")),
    otherCapitalAllowance = Option(BigDecimal("300.11")),
    zeroEmissionGoodsVehicleAllowance = Option(BigDecimal("405.11")),
    businessPremisesRenovationAllowance = Option(BigDecimal("550.11")),
    costOfReplacingDomesticGoods = Option(BigDecimal("550.11")),
    propertyIncomeAllowance = Option(BigDecimal("550.11"))
  )

  private val response =
    RetrieveHistoricNonFhlUkPropertyAnnualSubmissionResponse(Some(annualAdjustments), Some(annualAllowances))

  private val request = RetrieveHistoricNonFhlUkPropertyAnnualSubmissionRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  trait Test extends MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector {
    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveHistoricNonFhlUkPropertyAnnualSubmissionService(
      connector = mockRetrieveHistoricNonFhlUkPropertyConnector
    )
  }

  "retrieve" should {
    "return a success result" when {
      "a valid result is found" in new Test {
        MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector
          .retrieve(request) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        private val result = await(service.retrieve(request))
        result shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "return relevant mtd error according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveHistoricNonFhlUkPropertyAnnualSubmissionConnector
            .retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          private val result = await(service.retrieve(request))
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
