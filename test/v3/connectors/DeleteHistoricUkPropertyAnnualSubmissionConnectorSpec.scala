/*
 * Copyright 2023 HM Revenue & Customs
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

package v3.connectors

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import play.api.libs.json.JsObject
import api.models.domain.{HistoricPropertyType, Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import mocks.MockFeatureSwitches
import v3.models.request.deleteHistoricUkPropertyAnnualSubmission.DeleteHistoricUkPropertyAnnualSubmissionRequestData

import scala.concurrent.Future

class DeleteHistoricUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec with MockFeatureSwitches {

  private val nino    = Nino("AA123456A")
  private val taxYear = TaxYear.fromMtd("2021-22")

  "connector" must {
    "send a request and return no content" when {
      "using FHL data" in new IfsTest with Test {
        lazy val propertyType: HistoricPropertyType                    = HistoricPropertyType.Fhl
        override lazy val requiredHeaders: scala.Seq[(String, String)] = super.requiredHeaders :+ ("intent" -> "DELETE")

        MockFeatureSwitches.isPassIntentEnabled.returns(true)

        willPut(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/furnished-holiday-lettings/annual-summaries/2022",
          body = JsObject.empty
        ).returns(Future.successful(expectedOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteHistoricUkPropertyAnnualSubmission(request))

        result shouldBe expectedOutcome
      }

      "using non-FHL data" in new IfsTest with Test {
        lazy val propertyType: HistoricPropertyType                    = HistoricPropertyType.NonFhl
        override lazy val requiredHeaders: scala.Seq[(String, String)] = super.requiredHeaders :+ ("intent" -> "DELETE")

        MockFeatureSwitches.isPassIntentEnabled.returns(true)

        willPut(
          url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/2022",
          body = JsObject.empty
        ).returns(Future.successful(expectedOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteHistoricUkPropertyAnnualSubmission(request))

        result shouldBe expectedOutcome
      }

      "isPassIntentHeader feature switch is off" in new IfsTest with Test {
        override lazy val excludedHeaders: scala.Seq[(String, String)] = super.excludedHeaders :+ ("intent" -> "DELETE")
        lazy val propertyType: HistoricPropertyType                    = HistoricPropertyType.NonFhl

        MockFeatureSwitches.isPassIntentEnabled returns false

        willPut(url = s"$baseUrl/income-tax/nino/$nino/uk-properties/other/annual-summaries/2022", body = JsObject.empty)
          .returns(Future.successful(expectedOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.deleteHistoricUkPropertyAnnualSubmission(request))

        result shouldBe expectedOutcome

      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val propertyType: HistoricPropertyType

    val connector: DeleteHistoricUkPropertyAnnualSubmissionConnector = new DeleteHistoricUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request: DeleteHistoricUkPropertyAnnualSubmissionRequestData =
      DeleteHistoricUkPropertyAnnualSubmissionRequestData(nino = nino, taxYear = taxYear, propertyType = propertyType)

    protected val expectedOutcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

  }

}
