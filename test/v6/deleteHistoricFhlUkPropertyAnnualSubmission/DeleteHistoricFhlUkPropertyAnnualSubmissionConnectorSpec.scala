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

package v6.deleteHistoricFhlUkPropertyAnnualSubmission

import common.models.domain.HistoricPropertyType
import play.api.Configuration
import play.api.libs.json.JsObject
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v6.deleteHistoricFhlUkPropertyAnnualSubmission.model.request.Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData

import scala.concurrent.Future

class DeleteHistoricFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")

  "connector" must {
    "send a request and return no content" when {
      "using FHL data" in new IfsTest with Test {
        def propertyType: HistoricPropertyType = HistoricPropertyType.Fhl

        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("passIntentHeader.enabled" -> true)

        willPut(
          url = url"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/2022",
          body = JsObject.empty
        ).returns(Future.successful(expectedOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteHistoricUkPropertyAnnualSubmission(request))

        result shouldBe expectedOutcome
      }

      "using non-FHL data" in new IfsTest with Test {
        def propertyType: HistoricPropertyType = HistoricPropertyType.NonFhl

        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("passIntentHeader.enabled" -> true)

        willPut(
          url = url"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/2022",
          body = JsObject.empty
        ).returns(Future.successful(expectedOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteHistoricUkPropertyAnnualSubmission(request))

        result shouldBe expectedOutcome
      }

      "isPassIntentHeader feature switch is off" in new IfsTest with Test {
        def propertyType: HistoricPropertyType = HistoricPropertyType.NonFhl

        MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration("passIntentHeader.enabled" -> true)

        willPut(url = url"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/2022", body = JsObject.empty)
          .returns(Future.successful(expectedOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteHistoricUkPropertyAnnualSubmission(request))

        result shouldBe expectedOutcome

      }
    }
  }

  trait Test {
    self: ConnectorTest =>

    protected def propertyType: HistoricPropertyType

    protected val taxYear: TaxYear = TaxYear.fromMtd("2021-22")

    val connector: DeleteHistoricFhlUkPropertyAnnualSubmissionConnector = new DeleteHistoricFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    protected val request: Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData =
      Def1_DeleteHistoricFhlUkPropertyAnnualSubmissionRequestData(nino = nino, taxYear = taxYear, propertyType = propertyType)

    protected val expectedOutcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

  }

}
