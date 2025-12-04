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

package v6.createForeignPropertyDetails

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.*
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.utils.JsonErrorValidators
import shared.support.IntegrationBaseSpec
import shared.services.*
import v6.createForeignPropertyDetails.def1.model.response.Def1_CreateForeignPropertyDetailsResponse
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import shared.models.errors.*

class Def1_CreateForeignPropertyDetailsISpec extends IntegrationBaseSpec with JsonErrorValidators {

  val validRequestBodyJson: JsValue = Json.parse(
    """{
      |"propertyName": "Bob & Bobby Co",
      |"countryCode": "FRA",
      |"endDate": "2026-08-24",
      |"endReason": "no-longer-renting-property-out"
      |}""".stripMargin
  )

  val def1_CreateForeignPropertyDetailsResponseModel: Def1_CreateForeignPropertyDetailsResponse = Def1_CreateForeignPropertyDetailsResponse(
    "8e8b8450-dc1b-4360-8109-7067337b42cb"
  )

  val def1_CreateForeignPropertyDetailsResponseJson: JsValue = Json.parse(
    """{
      |"propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb"
      |}""".stripMargin
  )

  private trait Test {
    val nino: String = "TC663795B"
    val businessId: String = "XAIS12345678910"
    val correlationId: String = "X-123"

    def taxYear: String

    def downstreamTaxYear: String

    def downstreamUri: String

    val requestBodyJson: JsValue = validRequestBodyJson

    def setupStubs(): StubMapping

    private def uri: String = s"/foreign/$nino/$businessId/details/$taxYear"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.6.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         | [
         |{
         |  "errorCode": "$code",
         |  "errorDescription": "hip message"
         |}
         |]
           """.stripMargin
  }

  private trait HipTest extends Test {
    def taxYear: String = "2026-27"

    def downstreamTaxYear: String = "26-27"

    def downstreamQueryParams: Map[String, String] = Map("taxYear" -> downstreamTaxYear)

    override def downstreamUri: String = s"/itsd/income-sources/$nino/foreign-property-details/$businessId"
  }

  "Calling the Create Foreign Property Details endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new HipTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, downstreamQueryParams, OK, def1_CreateForeignPropertyDetailsResponseJson)
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe def1_CreateForeignPropertyDetailsResponseJson
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestBusinessId: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new HipTest {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = List(
          ("AA123", "XAIS12345678910", "2026-27", validRequestBodyJson, BAD_REQUEST, NinoFormatError),
          ("AA123456A", "203100", "2026-27", validRequestBodyJson, BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", "2020", validRequestBodyJson, BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "XAIS12345678910", "2020-22", validRequestBodyJson, BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "XAIS12345678910", "2019-20", validRequestBodyJson, BAD_REQUEST, RuleTaxYearNotSupportedError)
        )
        input.foreach(args => validationErrorTest.tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new HipTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.POST, downstreamUri, downstreamQueryParams, downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request().post(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1007", BAD_REQUEST, BusinessIdFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1000", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
          (UNPROCESSABLE_ENTITY, "1245", BAD_REQUEST, RuleDuplicatePropertyNameError),
          (UNPROCESSABLE_ENTITY, "1246", BAD_REQUEST, RuleTaxYearBeforeBusinessStartError),
          (UNPROCESSABLE_ENTITY, "1247", BAD_REQUEST, RuleEndDateAfterTaxYearEndError),
          (UNPROCESSABLE_ENTITY, "1248", BAD_REQUEST, RulePropertyBusinessCeasedError),
          (UNPROCESSABLE_ENTITY, "1249", BAD_REQUEST, RuleMissingEndDetailsError),
          (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError),
          (NOT_IMPLEMENTED, "5000", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        errors.foreach(args => serviceErrorTest.tupled(args))
      }
    }
  }

}
