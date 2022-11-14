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

import v2.models.domain.{Nino, TaxYear}
import v2.models.outcomes.ResponseWrapper
import v2.models.request.createAmendForeignPropertyAnnualSubmission._

import scala.concurrent.Future

class CreateAmendForeignPropertyAnnualSubmissionConnectorSpec extends ConnectorSpec with CreateAmendForeignPropertyAnnualSubmissionFixture {

  val nino: String = "AA123456A"
  val businessId: String = "XAIS12345678910"
  val taxYear: String = "2020-21"

  val body: CreateAmendForeignPropertyAnnualSubmissionRequestBody = createAmendForeignPropertyAnnualSubmissionRequestBody

  val request: CreateAmendForeignPropertyAnnualSubmissionRequest = CreateAmendForeignPropertyAnnualSubmissionRequest(
    nino = Nino(nino),
    businessId = businessId,
    taxYear = TaxYear.fromMtd(taxYear),
    body = body
  )

  trait Test {    _: ConnectorTest =>
    val connector = new CreateAmendForeignPropertyAnnualSubmissionConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }

  "connector" must {
    "put a body and return a 204" in new IfsTest with Test {
      val outcome = Right(ResponseWrapper(correlationId, ()))

      willPut(
          url = s"$baseUrl/income-tax/business/property/annual?taxableEntityId=$nino&incomeSourceId=$businessId&taxYear=2020-21",
          body = body)
        .returns(Future.successful(outcome))

      await(connector.createAmendForeignPropertyAnnualSubmission(request)) shouldBe outcome

    }
  }
}
