/*
 * Copyright 2026 HM Revenue & Customs
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

package v6.listPropertyPeriodSummary.def1

import api.models.errors.*
import api.services.{AuthStub, DownstreamStub, MtdIdLookupStub}
import api.support.IntegrationBaseSpec
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{ACCEPT, AUTHORIZATION}

class Def1_ListPropertyPeriodSummariesIfsISpec extends IntegrationBaseSpec {

  override def servicesConfig: Map[String, Any] =
    Map("feature-switch.ifs_hip_migration_1954.enabled" -> false) ++ super.servicesConfig

  "Calling the List Property Period Summaries endpoint" should {
    "return a 200 status code" when {
      "any valid pre-TYS request is made" in new NonTysTest {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, Status.OK, downstreamResponse)

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe mtdResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid TYS request is made" in new TysIfsTest {

        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, downstreamQueryParams, Status.OK, downstreamResponse)

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe mtdResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "a validation error occurs" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new TysIfsTest {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String = requestTaxYear

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA123", "XAIS12345678910", "2023-24", Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", "2023-24", Status.BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2020", Status.BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2022-24", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2019-20", Status.BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => validationErrorTest.tupled(args))
      }

      "a downstream service error occurs" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new TysIfsTest {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request().get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
          (Status.BAD_REQUEST, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.BAD_REQUEST, "INVALID_CORRELATION_ID", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.NOT_FOUND, "NOT_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
        )

        errors.foreach(args => serviceErrorTest.tupled(args))
      }
    }
  }

  private trait Test {
    val nino: String = "AA123456A"
    val businessId: String = "XAIS12345678910"

    def taxYear: String

    val mtdResponse: JsValue = Json.parse(
      s"""
         |{
         |  "submissions": [
         |    {
         |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
         |      "fromDate": "2022-06-22",
         |      "toDate": "2023-06-22"
         |    }
         |  ]
         |}
         |""".stripMargin
    )


    val downstreamResponse: JsValue = Json.parse(
      s"""
         |[
         |  {
         |    "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
         |    "fromDate": "2022-06-22",
         |    "toDate": "2023-06-22"
         |  }
         |]
         |""".stripMargin
    )

    def setupStubs(): Unit = ()

    def downstreamUri: String

    def downstreamQueryParams: Map[String, String]

    def request(): WSRequest = {
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/$businessId/period/$taxYear")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.6.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "failures": [
         |    {
         |      "code": "$code",
         |      "reason": "message"
         |    }
         |  ]
         |}
         |""".stripMargin
  }

  private trait NonTysTest extends Test {
    def taxYear: String = "2021-22"

    def downstreamUri: String = s"/income-tax/business/property/$nino/$businessId/period"

    def downstreamQueryParams: Map[String, String] = Map(
      "taxYear"         -> "2021-22"
    )

  }

  private trait TysIfsTest extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/business/property/23-24/$nino/$businessId/period"

    def downstreamQueryParams: Map[String, String] = Map.empty
  }

}
