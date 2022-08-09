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

package v2.controllers

import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.hateoas.MockHateoasFactory
import v2.mocks.requestParsers.MockCreateHistoricFhlUkPiePeriodSummaryRequestParser
import v2.mocks.services.{ MockCreateHistoricFhlUkPiePeriodSummaryService, MockEnrolmentsAuthService, MockMtdIdLookupService }
import v2.models.domain.Nino
import v2.models.hateoas.{ HateoasWrapper, Link }
import v2.models.hateoas.Method.GET
import v2.models.outcomes.ResponseWrapper
import v2.models.request.createHistoricFhlUkPiePeriodSummary.{
  CreateHistoricFhlUkPiePeriodSummaryRawData,
  CreateHistoricFhlUkPiePeriodSummaryRequest,
  CreateHistoricFhlUkPiePeriodSummaryRequestBody
}
import v2.models.response.createHistoricFhlUkPiePeriodSummary.{
  CreateHistoricFhlUkPiePeriodSummaryHateoasData,
  CreateHistoricFhlUkPiePeriodSummaryResponse
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateHistoricFhlUkPiePeriodSummaryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateHistoricFhlUkPiePeriodSummaryService
    with MockCreateHistoricFhlUkPiePeriodSummaryRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino: String                 = "AA123456A"
  private val correlationId: String        = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val transactionReference: String = "transaction-reference"
  private val periodId: String             = "2021-01-01_2021-01-02"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: CreateHistoricFhlUkPiePeriodSummaryController = new CreateHistoricFhlUkPiePeriodSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateHistoricFhlUkPiePeriodSummaryRequestParser,
      service = mockCreateHistoricFhlUkPiePeriodSummaryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    val requestBody: CreateHistoricFhlUkPiePeriodSummaryRequestBody =
      CreateHistoricFhlUkPiePeriodSummaryRequestBody("2021-01-01", "2021-01-02", None, None)

    private val requestBodyJson: JsValue = Json.parse(
      """{
        |    "fromDate": "2021-01-01",
        |    "toDate": "2021-01-02"
        |}
        |""".stripMargin
    )

    private val requestData: CreateHistoricFhlUkPiePeriodSummaryRequest = CreateHistoricFhlUkPiePeriodSummaryRequest(Nino(nino), requestBody)
    private val rawData: CreateHistoricFhlUkPiePeriodSummaryRawData     = CreateHistoricFhlUkPiePeriodSummaryRawData(nino, requestBodyJson)

    MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getCorrelationId.returns(correlationId)

    private val hateoasLinks: Seq[Link]  = Seq(Link(href = "/the-link/", method = GET, rel = "the-rel"))
    private val hateoasResponse: JsValue = Json.parse(s"""
         |{
         |  "links":[{
         |    "href": "/the-link",
         |    "method": "GET",
         |    "rel":"the-rel"
         |  }]         
         |}
         |""".stripMargin)

    private val response: CreateHistoricFhlUkPiePeriodSummaryResponse = CreateHistoricFhlUkPiePeriodSummaryResponse(transactionReference, None)

    "Create" should {
      "return a successful response " when {
        "the request received is valid" in new Test {
          MockCreateHistoricFhlUkPiePeriodSummaryRequestParser
            .parseRequest(rawData)
            .returns(Right(requestData))

          MockCreateHistoricFhlUkPiePeriodSummaryService
            .createPeriodSummary(requestData)
            .returns(
              Future.successful(Right(ResponseWrapper(correlationId, response)))
            )

          MockHateoasFactory
            .wrap(response, CreateHistoricFhlUkPiePeriodSummaryHateoasData(nino, periodId, transactionReference))
            .returns(HateoasWrapper(response, hateoasLinks))

          val result: Future[Result] = controller.handleRequest(nino)(fakeRequestWithBody(requestBodyJson))

          contentAsJson(result) shouldBe hateoasResponse
          status(result) shouldBe CREATED
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }
      }

    }
  }

}
