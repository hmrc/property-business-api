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
import play.api.libs.json.*
import play.api.test.Helpers.*
import shared.models.utils.JsonErrorValidators
import shared.support.IntegrationBaseSpec
import shared.services.*
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.libs.ws.{WSRequest, WSResponse}
import shared.models.errors.*

class Def1_CreateForeignPropertyDetailsISpec extends IntegrationBaseSpec with JsonErrorValidators {

  val requestBody: JsValue = Json.parse(
    """
      |{
      |  "propertyName": "Bob & Bobby Co",
      |  "countryCode": "FRA",
      |  "endDate": "2026-08-24",
      |  "endReason": "no-longer-renting-property-out"
      |}
    """.stripMargin
  )

  val responseBody: JsValue = Json.parse(
    """
      |{
      |  "propertyId": "8e8b8450-dc1b-4360-8109-7067337b42cb"
      |}
    """.stripMargin
  )

  private trait Test {
    val nino: String = "TC663795B"
    val businessId: String = "XAIS12345678910"
    val correlationId: String = "X-123"

    def taxYear: String = "2026-27"

    def downstreamQueryParams: Map[String, String] =  Map("taxYear" -> "26-27")

    def downstreamUri: String = s"/itsd/income-sources/$nino/foreign-property-details/$businessId"

    val requestBodyJson: JsValue = requestBody

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
        |[
        |  {
        |    "errorCode": "$code",
        |    "errorDescription": "string"
        |  }
        |]
      """.stripMargin
  }

  "Calling the Create Foreign Property Details endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.POST, downstreamUri, downstreamQueryParams, OK, responseBody)
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
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
                                expectedBody: MtdError,
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new Test {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().post(requestBody))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val validNino: String = "AA123456A"
        val validBusinessId: String = "XAIS12345678910"
        val validTaxYear: String = "2026-27"

        val input = List(
          ("AA1123A", validBusinessId, validTaxYear, requestBody, BAD_REQUEST, NinoFormatError, None),
          (validNino, validBusinessId, "2020", requestBody, BAD_REQUEST, TaxYearFormatError, None),
          (validNino, "203100", validTaxYear, requestBody, BAD_REQUEST, BusinessIdFormatError, None),
          (validNino, validBusinessId, "2026-28", requestBody, BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          (validNino, validBusinessId, "2025-26", requestBody, BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          (validNino, validBusinessId, validTaxYear, requestBody.update("/propertyName", JsString("")), BAD_REQUEST, PropertyNameFormatError, None),
          (validNino, validBusinessId, validTaxYear, requestBody.update("/countryCode", JsString("FRANCE")), BAD_REQUEST, CountryCodeFormatError, None),
          (validNino, validBusinessId, validTaxYear, requestBody.update("/endDate", JsString("24-08-2026")), BAD_REQUEST, EndDateFormatError, None),
          (validNino, validBusinessId, validTaxYear, requestBody.update("/endReason", JsString("invalid")), BAD_REQUEST, EndReasonFormatError, None),
          (validNino, validBusinessId, validTaxYear, JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          (validNino, validBusinessId, validTaxYear, requestBody.update("/countryCode", JsString("ABC")), BAD_REQUEST, RuleCountryCodeError, None),
          (validNino, validBusinessId, validTaxYear, requestBody.update("/endDate", JsString("2026-03-31")), BAD_REQUEST, RuleEndDateBeforeTaxYearStartError, None),
          (validNino, validBusinessId, validTaxYear, requestBody.update("/endDate", JsString("2027-05-24")), BAD_REQUEST, RuleEndDateAfterTaxYearEndError, None),
          (validNino, validBusinessId, validTaxYear, requestBody.removeProperty("/endDate"), BAD_REQUEST, RuleMissingEndDetailsError, Some("for missing end date")),
          (validNino, validBusinessId, validTaxYear, requestBody.removeProperty("/endReason"), BAD_REQUEST, RuleMissingEndDetailsError, Some("for missing end reason"))
        )

        input.foreach(args => validationErrorTest.tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

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
