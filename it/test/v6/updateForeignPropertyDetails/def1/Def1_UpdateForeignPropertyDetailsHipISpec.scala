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

package v6.updateForeignPropertyDetails.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.*
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.*
import play.api.libs.json.*
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSBodyWritables.{writeableOf_JsValue, writeableOf_String}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors.*
import shared.models.utils.JsonErrorValidators
import shared.services.*
import shared.support.IntegrationBaseSpec

class Def1_UpdateForeignPropertyDetailsHipISpec extends IntegrationBaseSpec with JsonErrorValidators {

  private def requestBody(propertyName: String = "Bob & Bobby Co",
                          endDate: String = "2026-08-24",
                          endReason: String = "disposal"): JsValue =
    Json.parse(s"""
                  |{
                  |    "propertyName": "$propertyName",
                  |    "endDate": "$endDate",
                  |    "endReason": "$endReason"
                  |}""".stripMargin)

  "calling the create and amend endpoint" should {

    "return a 204 status" when {

      "any valid request is made" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub
            .when(method = DownstreamStub.PUT, uri = downstreamUri)
            .thenReturn(status = NO_CONTENT, None)
        }

        val response: WSResponse = await(request().put(requestBody()))
        response.status shouldBe NO_CONTENT
        response.body shouldBe ""
        response.header("Content-Type") shouldBe None
      }
    }

    "return bad request error" when {
      "incorrect json body is used" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request().addHttpHeaders(("Content-Type", "application/json")).put("{ badJson }"))
        response.json shouldBe Json.toJson(BadRequestError)
        response.status shouldBe BAD_REQUEST
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestPropertyId: String,
                                requestTaxYear: String,
                                requestBody: JsValue,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                scenario: Option[String]): Unit = {
          s"validation fails with ${expectedBody.code} error ${scenario.getOrElse("")}" in new Test {

            override val nino: String = requestNino
            override val propertyId: String = requestPropertyId
            override val mtdTaxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request().put(requestBody))
            response.json shouldBe Json.toJson(expectedBody)
            response.status shouldBe expectedStatus
          }
        }

        val validNino: String = "AA999999A"
        val validPropertyId: String = "8e8b8450-dc1b-4360-8109-7067337b42cb"
        val validTaxYear: String = "2026-27"

        val missingEndDateJson = Json.parse(
          s"""
             |{
             |    "propertyName": "Bob & Bobby Co",
             |    "endReason": "disposal"
             |}
             |""".stripMargin)

        val missingEndReasonJson = Json.parse(
          s"""
             |{
             |    "propertyName": "Bob & Bobby Co",
             |    "endDate": "2026-08-24"
             |}
             |""".stripMargin)

        val input = List(
          ("AA1123A", validPropertyId, validTaxYear, requestBody(), BAD_REQUEST, NinoFormatError, None),
          (validNino, validPropertyId, "20267", requestBody(), BAD_REQUEST, TaxYearFormatError, None),
          (validNino, "8e8-db-60-89-706733*", validTaxYear, requestBody(), BAD_REQUEST, PropertyIdFormatError, None),
          (validNino, validPropertyId, "2026-28", requestBody(), BAD_REQUEST, RuleTaxYearRangeInvalidError, None),
          (validNino, validPropertyId, "2025-26", requestBody(), BAD_REQUEST, RuleTaxYearNotSupportedError, None),
          (validNino, validPropertyId, validTaxYear, requestBody(propertyName = ""), BAD_REQUEST, PropertyNameFormatError, None),
          (validNino, validPropertyId, validTaxYear, requestBody(endDate = "24-08-2026"), BAD_REQUEST, EndDateFormatError, None),
          (validNino, validPropertyId, validTaxYear, requestBody(endReason = "invalid"), BAD_REQUEST, EndReasonFormatError, None),
          (validNino, validPropertyId, validTaxYear, JsObject.empty, BAD_REQUEST, RuleIncorrectOrEmptyBodyError, None),
          (validNino, validPropertyId, validTaxYear, requestBody(endDate = "2027-05-24"), BAD_REQUEST, RuleEndDateAfterTaxYearEndError, None),
          (validNino, validPropertyId, validTaxYear, missingEndDateJson, BAD_REQUEST, RuleMissingEndDetailsError, Some("for missing end date")),
          (validNino, validPropertyId, validTaxYear, missingEndReasonJson, BAD_REQUEST, RuleMissingEndDetailsError, Some("for missing end reason"))
        )
        input.foreach(args => validationErrorTest.tupled(args))
      }
    }

    "downstream service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))
          }

          val response: WSResponse = await(request().put(requestBody()))
          response.json shouldBe Json.toJson(expectedBody)
          response.status shouldBe expectedStatus
        }
      }

      val errors = List(
        (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "1244", BAD_REQUEST, PropertyIdFormatError),
        (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "1000", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "1245", BAD_REQUEST, RuleDuplicatePropertyNameError),
        (UNPROCESSABLE_ENTITY, "1246", BAD_REQUEST, RulePropertyOutsidePeriodError),
        (UNPROCESSABLE_ENTITY, "1247", BAD_REQUEST, RuleEndDateAfterTaxYearEndError),
        (UNPROCESSABLE_ENTITY, "1248", BAD_REQUEST, RulePropertyBusinessCeasedError),
        (UNPROCESSABLE_ENTITY, "1249", BAD_REQUEST, RuleMissingEndDetailsError),
        (UNPROCESSABLE_ENTITY, "4200", BAD_REQUEST, RuleOutsideAmendmentWindowError),
        (NOT_IMPLEMENTED, "5000", BAD_REQUEST, RuleHistoricTaxYearNotSupportedError)
      )

      errors.foreach(args => serviceErrorTest.tupled(args))
    }
  }

  private trait Test {

    val nino: String = "AA999999A"
    val propertyId: String = "8e8b8450-dc1b-4360-8109-7067337b42cb"

    def mtdTaxYear: String = "2026-27"

    def setupStubs(): StubMapping

    def downstreamUri: String = s"/itsd/income-sources/$nino/foreign-property-details/$propertyId"

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/foreign/$nino/$propertyId/details/$mtdTaxYear")
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
}
