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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockAmendForeignPropertyPeriodSummaryConnector
import v1.models.domain.Nino
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendForeignPropertyPeriodSummary._
import v1.models.request.common.foreignFhlEea._
import v1.models.request.common.foreignPropertyEntry._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendForeignPropertyPeriodSummaryServiceSpec extends UnitSpec {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  implicit val correlationId: String = "X-123"

  private val foreignFhlEea: ForeignFhlEea = ForeignFhlEea(
    income = Some(ForeignFhlEeaIncome(Some(567.83))),
    expenditure = Some(ForeignFhlEeaExpenditure(
      premisesRunningCosts = Some(4567.98),
      repairsAndMaintenance = Some(98765.67),
      financialCosts = Some(4566.95),
      professionalFees = Some(23.65),
      costsOfServices = Some(4567.77),
      travelCosts = Some(456.77),
      other = Some(567.67),
      consolidatedExpenses = Some(456.98)
    ))
  )

  private val foreignProperty: ForeignPropertyEntry = ForeignPropertyEntry(
    countryCode = "zzz",
    income = Some(ForeignPropertyIncome(
      rentIncome = Some(ForeignPropertyRentIncome(rentAmount = Some(34456.30))),
      foreignTaxCreditRelief = true,
      premiumOfLeaseGrant = Some(2543.43),
      otherPropertyIncome = Some(54325.30),
      foreignTaxTakenOff = Some(6543.01),
      specialWithholdingTaxOrUKTaxPaid = Some(643245.00)
    )),
    expenditure = Some(ForeignPropertyExpenditure(
      premisesRunningCosts = Some(5635.43),
      repairsAndMaintenance = Some(3456.65),
      financialCosts = Some(34532.21),
      professionalFees = Some(32465.32),
      costsOfServices = Some(2567.21),
      travelCosts = Some(2345.76),
      residentialFinancialCost = Some(21235.22),
      broughtFwdResidentialFinancialCost = Some(12556.00),
      other = Some(2425.11),
      consolidatedExpenses = Some(352.66)
    ))
  )

  val body: AmendForeignPropertyPeriodSummaryRequestBody = AmendForeignPropertyPeriodSummaryRequestBody(
    foreignFhlEea = Some(foreignFhlEea),
    foreignProperty = Some(Seq(foreignProperty))
  )

  private val requestData = AmendForeignPropertyPeriodSummaryRequest(Nino(nino), businessId, submissionId, body)

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