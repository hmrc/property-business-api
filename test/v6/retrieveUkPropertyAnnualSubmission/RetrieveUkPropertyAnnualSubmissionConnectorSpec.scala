/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveUkPropertyAnnualSubmission

import org.scalamock.handlers.CallHandler
import play.api.Configuration
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{BusinessId, Nino, TaxYear, Timestamp}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.retrieveUkPropertyAnnualSubmission.model.{NonUkResult, Result, UkResult}
import v6.retrieveUkPropertyAnnualSubmission.def1.model.request.Def1_RetrieveUkPropertyAnnualSubmissionRequestData
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.Def1_RetrieveUkPropertyAnnualSubmissionResponse
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukFhlProperty.{RetrieveUkFhlProperty => RetrieveUkFhlPropertyDef1}
import v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty.{RetrieveUkProperty => RetrieveUkPropertyDef1}
import v6.retrieveUkPropertyAnnualSubmission.def2.model.request.Def2_RetrieveUkPropertyAnnualSubmissionRequestData
import v6.retrieveUkPropertyAnnualSubmission.def2.model.response.Def2_RetrieveUkPropertyAnnualSubmissionResponse
import v6.retrieveUkPropertyAnnualSubmission.def2.model.response.{RetrieveUkProperty => RetrieveUkPropertyDef2}
import v6.retrieveUkPropertyAnnualSubmission.model.response.*

import scala.concurrent.Future

class RetrieveUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val businessId = BusinessId("XAIS12345678910")

  private val ukFhlPropertyDef1 = RetrieveUkFhlPropertyDef1(None, None)
  private val ukPropertyDef1    = RetrieveUkPropertyDef1(None, None)
  private val ukPropertyDef2    = RetrieveUkPropertyDef2(None, None)

  "connector" when {
    "response has uk fhl details" must {
      "return a uk result" in new StandardIfsTest {
        testReturnUkResultWithFhlDetails(false)
      }
    }

    "response has uk non-fhl details" must {
      "return a uk result" in new StandardIfsTest {
        setIfsHipMigration1805Enabled(false)

        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse = responseWith(ukFhlProperty = None, ukProperty = Some(ukPropertyDef1))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has uk fhl and non-fhl details" must {
      "return a uk result" in new StandardIfsTest {
        setIfsHipMigration1805Enabled(false)

        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
          responseWith(ukFhlProperty = Some(ukFhlPropertyDef1), ukProperty = Some(ukPropertyDef1))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "response has no details" must {
      "return a non-uk result" in new StandardIfsTest {
        setIfsHipMigration1805Enabled(false)

        val response: RetrieveUkPropertyAnnualSubmissionResponse                                 = responseWith(None, None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, NonUkResult))
      }
    }

    "response is an error" must {
      "return the error" in new StandardIfsTest {
        setIfsHipMigration1805Enabled(false)

        val outcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))))
      }
    }

    "request is for a pre-TYS tax year" must {
      "use the pre-TYS URL" in new IfsTest with Test {
        setIfsHipMigration1805Enabled(false)

        def taxYear: String = "2019-20"

        val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
          responseWith(Some(ukFhlPropertyDef1), ukProperty = None)
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

        willGet(
          url = url"$baseUrl/income-tax/business/property/annual",
          parameters = List("taxableEntityId" -> nino.nino, "incomeSourceId" -> businessId.businessId, "taxYear" -> taxYear)
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }
    }

    "ifs_hip_migration_1805.enabled is true" must {
      "use HIP downstream when tax year greater than or equal to 2025-26" in new StandardHipTest {
        setIfsHipMigration1805Enabled(true)

        val response: Def2_RetrieveUkPropertyAnnualSubmissionResponse = responseWith(ukProperty = Some(ukPropertyDef2))
        val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] =
          Right(ResponseWrapper(correlationId, response))

        stubHttpResponse(outcome)

        val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
        result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
      }

      "use IFS downstream when tax year less than 2025-26" in new StandardIfsTest {
        testReturnUkResultWithFhlDetails(true)
      }
    }
  }

  trait Test {
    self: ConnectorTest =>

    protected def taxYear: String

    protected val connector: RetrieveUkPropertyAnnualSubmissionConnector = new RetrieveUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: Def1_RetrieveUkPropertyAnnualSubmissionRequestData =
      Def1_RetrieveUkPropertyAnnualSubmissionRequestData(nino, businessId, TaxYear.fromMtd(taxYear))

    def responseWith(ukFhlProperty: Option[RetrieveUkFhlPropertyDef1],
                     ukProperty: Option[RetrieveUkPropertyDef1]): Def1_RetrieveUkPropertyAnnualSubmissionResponse =
      Def1_RetrieveUkPropertyAnnualSubmissionResponse(Timestamp("2022-06-17T10:53:38Z"), ukFhlProperty, ukProperty)

    def setIfsHipMigration1805Enabled(ifsHipMigration1805Enabled: Boolean): Unit = {
      MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1805.enabled" -> ifsHipMigration1805Enabled)
    }

  }

  trait StandardIfsTest extends IfsTest with Test {
    protected def taxYear = "2023-24"

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveUkPropertyAnnualSubmissionResponse])
        : CallHandler[Future[DownstreamOutcome[RetrieveUkPropertyAnnualSubmissionResponse]]]#Derived = {
      willGet(
        url = url"$baseUrl/income-tax/business/property/annual/23-24/$nino/$businessId"
      ).returns(Future.successful(outcome))
    }

    def testReturnUkResultWithFhlDetails(setFeatureSwitch1805: Boolean): Unit = {
      setIfsHipMigration1805Enabled(setFeatureSwitch1805)

      val response: Def1_RetrieveUkPropertyAnnualSubmissionResponse =
        responseWith(ukFhlProperty = Some(ukFhlPropertyDef1), ukProperty = None)
      val outcome: Right[Nothing, ResponseWrapper[RetrieveUkPropertyAnnualSubmissionResponse]] = Right(ResponseWrapper(correlationId, response))

      stubHttpResponse(outcome)

      val result: DownstreamOutcome[Result] = await(connector.retrieveUkProperty(request))
      result shouldBe Right(ResponseWrapper(correlationId, UkResult(response)))
    }

  }

  trait StandardHipTest extends HipTest {
    self: ConnectorTest =>

    protected def taxYear: String = "2025-26"

    protected val connector: RetrieveUkPropertyAnnualSubmissionConnector = new RetrieveUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: Def2_RetrieveUkPropertyAnnualSubmissionRequestData =
      Def2_RetrieveUkPropertyAnnualSubmissionRequestData(nino, businessId, TaxYear.fromMtd(taxYear))

    def responseWith(ukProperty: Option[RetrieveUkPropertyDef2]): Def2_RetrieveUkPropertyAnnualSubmissionResponse =
      Def2_RetrieveUkPropertyAnnualSubmissionResponse(Timestamp(f"${taxYear.take(4)}-06-17T10:53:38Z"), ukProperty)

    def stubHttpResponse(outcome: DownstreamOutcome[RetrieveUkPropertyAnnualSubmissionResponse])
        : CallHandler[Future[DownstreamOutcome[RetrieveUkPropertyAnnualSubmissionResponse]]]#Derived = {
      willGet(
        url = url"$baseUrl/income-tax/v1/${taxYear.drop(2)}/business/property/annual/$nino/$businessId"
      ).returns(Future.successful(outcome))
    }

    def setIfsHipMigration1805Enabled(ifsHipMigration1805Enabled: Boolean): Unit = {
      MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1805.enabled" -> ifsHipMigration1805Enabled)
    }

  }

}
