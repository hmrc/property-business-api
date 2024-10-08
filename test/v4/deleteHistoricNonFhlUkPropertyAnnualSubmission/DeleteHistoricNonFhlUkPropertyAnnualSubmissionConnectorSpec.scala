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

package v4.deleteHistoricNonFhlUkPropertyAnnualSubmission

import api.connectors.{ConnectorSpec, DownstreamOutcome}
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import mocks.MockFeatureSwitches
import play.api.libs.json.JsObject
import v4.deleteHistoricNonFhlUkPropertyAnnualSubmission.model.request.{
  Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData,
  DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData
}

import scala.concurrent.Future

class DeleteHistoricNonFhlUkPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec with MockFeatureSwitches {

  private val nino    = Nino("AA123456A")
  private val taxYear = TaxYear.fromMtd("2021-22")

  "connector" must {
    "send a request and return no content" when {

      "sending a non-FHL request" in new IfsTest with Test {
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

    val connector: DeleteHistoricNonFhlUkPropertyAnnualSubmissionConnector = new DeleteHistoricNonFhlUkPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected val request: DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData =
      Def1_DeleteHistoricNonFhlUkPropertyAnnualSubmissionRequestData(nino = nino, taxYear = taxYear)

    protected val expectedOutcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))
  }

}
