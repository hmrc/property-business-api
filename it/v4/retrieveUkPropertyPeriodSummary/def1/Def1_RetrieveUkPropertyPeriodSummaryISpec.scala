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

package v4.retrieveUkPropertyPeriodSummary.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.models.errors.SubmissionIdFormatError
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.domain.TaxYear
import shared.models.errors._
import shared.services._
import shared.support.IntegrationBaseSpec

class Def1_RetrieveUkPropertyPeriodSummaryISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    def taxYear: String
    val businessId: String   = "XAIS12345678910"
    val submissionId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "submittedOn": "2020-06-17T10:53:38.000Z",
         |  "fromDate": "2019-01-29",
         |  "toDate": "2020-03-29",
         |  "ukFhlProperty": {
         |    "income": {
         |      "periodAmount": 1.11,
         |      "taxDeducted": 1.12,
         |      "rentARoom": {
         |        "rentsReceived": 1.13
         |      }
         |    },
         |    "expenses": {
         |      "premisesRunningCosts": 2.11,
         |      "repairsAndMaintenance": 2.12,
         |      "financialCosts": 2.13 ,
         |      "professionalFees": 2.14,
         |      "costOfServices": 2.15,
         |      "other": 2.16,
         |      "consolidatedExpenses": 2.17,
         |      "travelCosts": 2.18,
         |      "rentARoom": {
         |        "amountClaimed": 2.19
         |      }
         |    }
         |  },
         |  "ukNonFhlProperty": {
         |    "income": {
         |      "premiumsOfLeaseGrant": 3.11,
         |      "reversePremiums": 3.12,
         |      "periodAmount": 3.13,
         |      "taxDeducted": 3.14,
         |      "otherIncome": 3.15,
         |      "rentARoom": {
         |        "rentsReceived": 3.16
         |      }
         |    },
         |    "expenses": {
         |      "premisesRunningCosts": 4.11,
         |      "repairsAndMaintenance": 4.12,
         |      "financialCosts": 4.13,
         |      "professionalFees": 4.14,
         |      "costOfServices": 4.15,
         |      "other": 4.16,
         |      "consolidatedExpenses": 4.17,
         |      "residentialFinancialCost": 4.18,
         |      "travelCosts": 4.19,
         |      "residentialFinancialCostsCarriedForward": 4.20,
         |      "rentARoom": {
         |        "amountClaimed": 4.21
         |      }
         |    }
         |  }
         |}
       """.stripMargin
    )

    val downstreamResponseBody: JsValue = Json.parse(
      """
        |{
        |  "submittedOn": "2020-06-17T10:53:38.000Z",
        |  "fromDate": "2019-01-29",
        |  "toDate": "2020-03-29",
        |  "ukFhlProperty": {
        |    "income": {
        |      "periodAmount": 1.11,
        |      "taxDeducted": 1.12,
        |      "ukFhlRentARoom": {
        |        "rentsReceived": 1.13
        |      }
        |    },
        |    "expenses": {
        |      "premisesRunningCosts": 2.11,
        |      "repairsAndMaintenance": 2.12,
        |      "financialCosts": 2.13 ,
        |      "professionalFees": 2.14,
        |      "costOfServices": 2.15,
        |      "other": 2.16,
        |      "consolidatedExpense": 2.17,
        |      "travelCosts": 2.18,
        |      "ukFhlRentARoom": {
        |        "amountClaimed": 2.19
        |      }
        |    }
        |  },
        |  "ukOtherProperty": {
        |    "income": {
        |      "premiumsOfLeaseGrant": 3.11,
        |      "reversePremiums": 3.12,
        |      "periodAmount": 3.13,
        |      "taxDeducted": 3.14,
        |      "otherIncome": 3.15,
        |      "ukOtherRentARoom": {
        |        "rentsReceived": 3.16
        |      }
        |    },
        |    "expenses": {
        |      "premisesRunningCosts": 4.11,
        |      "repairsAndMaintenance": 4.12,
        |      "financialCosts": 4.13,
        |      "professionalFees": 4.14,
        |      "costOfServices": 4.15,
        |      "other": 4.16,
        |      "consolidatedExpense": 4.17,
        |      "residentialFinancialCost": 4.18,
        |      "travelCosts": 4.19,
        |      "residentialFinancialCostsCarriedForward": 4.20,
        |      "ukOtherRentARoom": {
        |        "amountClaimed": 4.21
        |      }
        |    }
        |  }
        |}
       """.stripMargin
    )

    def uri: String = s"/uk/$nino/$businessId/period/$taxYear/$submissionId"

    def ifsQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "taxYear"         -> taxYear,
      "incomeSourceId"  -> businessId,
      "submissionId"    -> submissionId
    )

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "code": "$code",
         |  "reason": "ifs message"
         |}
       """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String = "2022-23"

    def ifsUri: String = s"/income-tax/business/property/periodic"
  }

  private trait TysIfsTest extends Test {
    def taxYear: String = "2023-24"

    def tysUri: String = s"/income-tax/business/property/${TaxYear.fromMtd(taxYear).asTysDownstream}/$nino/$businessId/periodic/$submissionId"
  }

  "Retrieve UK property period summary endpoint" should {
    "return a 200 status code" when {
      "any valid request is made to the ifs endpoint" in new NonTysTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, ifsUri, ifsQueryParams, Status.OK, downstreamResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made to the tys endpoint" in new TysIfsTest {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, tysUri, Status.OK, downstreamResponseBody)
        }

        val response: WSResponse = await(request().get())
        response.status shouldBe Status.OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return validation error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestBusinessId: String,
                              requestTaxYear: String,
                              requestSubmissionId: String,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new NonTysTest {

          override val nino: String         = requestNino
          override val businessId: String   = requestBusinessId
          override val submissionId: String = requestSubmissionId
          override val taxYear: String      = requestTaxYear

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(requestNino)
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = List(
        ("AA1123A", "XAIS12345678910", "2022-23", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, NinoFormatError),
        ("AA123456A", "XAIS12345678910", "20223", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "XAIS12345678910", "2021-23", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("AA123456A", "XAIS12345678910", "2021-22", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
        ("AA123456A", "XA123", "2022-23", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, BusinessIdFormatError),
        ("AA123456A", "XAIS12345678910", "2022-23", "4557ecb5-48cc-81f5-e6acd1099f3c", Status.BAD_REQUEST, SubmissionIdFormatError)
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "return ifs service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"ifs returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.GET, ifsUri, downstreamStatus, errorBody(downstreamCode))
          }

          val response: WSResponse = await(request().get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val input = List(
        (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
        (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
        (Status.BAD_REQUEST, "TAX_YEAR_NOT_SUPPORTED", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
        (Status.BAD_REQUEST, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
        (Status.BAD_REQUEST, "INVALID_SUBMISSION_ID", Status.BAD_REQUEST, SubmissionIdFormatError),
        (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
        (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, NotFoundError),
        (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
        (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
      )

      val tysInput = List(
        (Status.BAD_REQUEST, "INVALID_INCOMESOURCE_ID", Status.BAD_REQUEST, BusinessIdFormatError),
        (Status.BAD_REQUEST, "INVALID_CORRELATION_ID", Status.INTERNAL_SERVER_ERROR, InternalError)
      )

      (input ++ tysInput).foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
