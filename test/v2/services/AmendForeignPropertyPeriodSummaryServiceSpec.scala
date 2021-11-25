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
import v2.mocks.connectors.MockAmendForeignPropertyPeriodSummaryConnector
import v2.models.domain.Nino
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendForeignPropertyPeriodSummary._
import v2.models.request.common.foreignFhlEea._
import v2.models.request.common.foreignPropertyEntry._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryServiceSpec extends UnitSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2022-23"
  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "X-123"

  private val foreignFhlEea: AmendForeignFhlEea = AmendForeignFhlEea(
    income = Some(ForeignFhlEeaIncome(rentAmount = Some(567.83))),
    expenses = Some(AmendForeignFhlEeaExpenses(
      premisesRunningCosts = Some(4567.98),
      repairsAndMaintenance = Some(98765.67),
      financialCosts = Some(4566.95),
      professionalFees = Some(23.65),
      costOfServices = Some(4567.77),
      travelCosts = Some(456.77),
      other = Some(567.67),
      consolidatedExpenses = Some(456.98)
    ))
  )

  private val foreignNonFhlProperty: AmendForeignNonFhlPropertyEntry = AmendForeignNonFhlPropertyEntry(
    countryCode = "zzz",
    income = Some(ForeignNonFhlPropertyIncome(
      rentIncome = ForeignNonFhlPropertyRentIncome(rentAmount = Some(34456.30)),
      foreignTaxCreditRelief = true,
      premiumsOfLeaseGrant = Some(2543.43),
      otherPropertyIncome = Some(54325.30),
      foreignTaxPaidOrDeducted = Some(6543.01),
      specialWithholdingTaxOrUkTaxPaid = Some(643245.00)
    )),
    expenses = Some(AmendForeignNonFhlPropertyExpenses(
      premisesRunningCosts = Some(5635.43),
      repairsAndMaintenance = Some(3456.65),
      financialCosts = Some(34532.21),
      professionalFees = Some(32465.32),
      costOfServices = Some(2567.21),
      travelCosts = Some(2345.76),
      residentialFinancialCost = Some(21235.22),
      broughtFwdResidentialFinancialCost = Some(12556.00),
      other = Some(2425.11),
      consolidatedExpenses = Some(352.66)
    ))
  )

  val body: AmendForeignPropertyPeriodSummaryRequestBody = AmendForeignPropertyPeriodSummaryRequestBody(
    foreignFhlEea = Some(foreignFhlEea),
    foreignNonFhlProperty = Some(Seq(foreignNonFhlProperty))
  )

  private val requestData = AmendForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, taxYear, submissionId, body)

  trait Test extends MockAmendForeignPropertyPeriodSummaryConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendForeignPropertyPeriodSummaryService(
      connector = mockAmendForeignPropertyConnector
    )
  }

  "service" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockAmendForeignPropertyConnector.amendForeignProperty(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        await(service.amendForeignProperty(requestData)) shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }
  }

  "unsuccessful" should {
    "map errors according to spec" when {

      def serviceError(ifsErrorCode: String, error: MtdError): Unit =
        s"a $ifsErrorCode error is returned from the service" in new Test {

          MockAmendForeignPropertyConnector.amendForeignProperty(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, IfsErrors.single(IfsErrorCode(ifsErrorCode))))))

          await(service.amendForeignProperty(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOMESOURCEID" -> BusinessIdFormatError,
        "INVALID_CORRELATIONID" -> DownstreamError,
        "INVALID_PAYLOAD" -> DownstreamError,
        "INVALID_SUBMISSION_ID" -> SubmissionIdFormatError,
        "INCOME_SOURCE_NOT_FOUND" -> NotFoundError,
        "NO_DATA_FOUND" -> NotFoundError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}