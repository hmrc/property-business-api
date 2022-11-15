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

package v2.connectors

import org.scalamock.handlers.CallHandler
import v2.models.domain.{ Nino, TaxYear }
import v2.models.errors.{ DownstreamErrorCode, DownstreamErrors }
import v2.models.outcomes.ResponseWrapper
import v2.models.request.amendUkPropertyAnnualSubmission._
import v2.models.request.amendUkPropertyAnnualSubmission.ukFhlProperty._
import v2.models.request.amendUkPropertyAnnualSubmission.ukNonFhlProperty._
import v2.models.request.common.ukPropertyRentARoom.UkPropertyAdjustmentsRentARoom
import v2.models.request.common.{ Building, FirstYear, StructuredBuildingAllowance }

import scala.concurrent.Future

class AmendUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  val nino: String          = "AA123456A"
  val businessId: String    = "XAIS12345678910"
  private val preTysTaxYear = TaxYear.fromMtd("2022-23")
  private val tysTaxYear    = TaxYear.fromMtd("2023-24")

  "AmendUkPropertyAnnualSubmissionConnector" when {
    val outcome = Right(ResponseWrapper(correlationId, ()))

    "amendUkPropertyAnnualSubmissionConnector" must {
      "put a body and return a 204" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result = await(connector.amendUkPropertyAnnualSubmission(request))

        result shouldBe outcome
      }
    }

    "amendUkPropertyAnnualSubmissionConnector called for a Tax Year Specific tax year" must {
      "put a body and return a 204" in new IfsTest with Test {
        def taxYear: TaxYear = tysTaxYear

        stubHttpResponse(outcome)

        val result = await(connector.amendUkPropertyAnnualSubmission(request))

        result shouldBe outcome
      }
    }

    "response is an error" must {

      val downstreamErrorResponse: DownstreamErrors =
        DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
      val outcome = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

      "return the error" in new IfsTest with Test {
        def taxYear: TaxYear = preTysTaxYear

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] =
          await(connector.amendUkPropertyAnnualSubmission(request))
        result shouldBe outcome
      }

      "return the error given a TYS tax year request" in new TysIfsTest with Test {
        def taxYear: TaxYear = tysTaxYear

        stubTysHttpResponse(outcome)

        val result: DownstreamOutcome[Unit] =
          await(connector.amendUkPropertyAnnualSubmission(request))
        result shouldBe outcome
      }
    }
  }

  private val ukFhlProperty = UkFhlProperty(
    Some(
      UkFhlPropertyAdjustments(
        Some(5000.99),
        Some(5000.99),
        periodOfGraceAdjustment = true,
        Some(5000.99),
        nonResidentLandlord = true,
        Some(UkPropertyAdjustmentsRentARoom(true))
      )),
    Some(
      UkFhlPropertyAllowances(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        None
      ))
  )

  private val ukNonFhlProperty = UkNonFhlProperty(
    Some(
      UkNonFhlPropertyAdjustments(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        nonResidentLandlord = true,
        Some(UkPropertyAdjustmentsRentARoom(true))
      )),
    Some(
      UkNonFhlPropertyAllowances(
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        Some(5000.99),
        None,
        Some(
          Seq(
            StructuredBuildingAllowance(
              5000.99,
              Some(FirstYear(
                "2020-01-01",
                5000.99
              )),
              Building(
                Some("Green Oak's"),
                None,
                "GF49JH"
              )
            ))),
        Some(
          Seq(
            StructuredBuildingAllowance(
              3000.50,
              Some(FirstYear(
                "2020-01-01",
                3000.60
              )),
              Building(
                None,
                Some("house number"),
                "GF49JH"
              )
            )))
      ))
  )

  val body: AmendUkPropertyAnnualSubmissionRequestBody = AmendUkPropertyAnnualSubmissionRequestBody(
    Some(ukFhlProperty),
    Some(ukNonFhlProperty)
  )

  trait Test {
    _: ConnectorTest =>
    def taxYear: TaxYear

    protected val connector: AmendUkPropertyAnnualSubmissionConnector = new AmendUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val request: AmendUkPropertyAnnualSubmissionRequest = AmendUkPropertyAnnualSubmissionRequest(
      nino = Nino(nino),
      businessId = businessId,
      taxYear = taxYear,
      body = body
    )

    protected def stubHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = s"$baseUrl/income-tax/business/property/annual?" +
          s"taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=${taxYear.asMtd}",
        body = body,
      ).returns(Future.successful(outcome))
    }

    protected def stubTysHttpResponse(outcome: DownstreamOutcome[Unit]): CallHandler[Future[DownstreamOutcome[Unit]]]#Derived = {
      willPut(
        url = s"$baseUrl/income-tax/business/property/annual/${taxYear.asTysDownstream}/$nino/$businessId",
        body = body,
      ).returns(Future.successful(outcome))
    }
  }
}
