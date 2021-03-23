/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class CreateForeignPropertyPeriodSummaryControllerISpec extends IntegrationBaseSpec {


  val unconsolidatedRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costsOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val consolidatedRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenditure": {
      |      "consolidatedExpenses": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "consolidatedExpenses": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val invalidToDateRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "20190406",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costsOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val invalidFromDateRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "20180406",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costsOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val invalidCountryCodeRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "ASDF",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costsOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val invalidValueRequestJson:  JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99634874383248236385
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99634874383248236385,
      |      "repairsAndMaintenance": 5000.99634874383248236385,
      |      "financialCosts": 5000.99634874383248236385,
      |      "professionalFees": 5000.99634874383248236385,
      |      "costsOfServices": 5000.99634874383248236385,
      |      "travelCosts": 5000.99634874383248236385,
      |      "other": 5000.99634874383248236385
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99634874383248236385
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99634874383248236385,
      |        "otherPropertyIncome": 5000.99634874383248236385,
      |        "foreignTaxTakenOff": 5000.99634874383248236385,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99634874383248236385
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99634874383248236385,
      |        "repairsAndMaintenance": 5000.99634874383248236385,
      |        "financialCosts": 5000.99634874383248236385,
      |        "professionalFees": 5000.99634874383248236385,
      |        "costsOfServices": 5000.99634874383248236385,
      |        "travelCosts": 5000.99634874383248236385,
      |        "residentialFinancialCost": 5000.99634874383248236385,
      |        "broughtFwdResidentialFinancialCost": 5000.99634874383248236385,
      |        "other": 5000.99634874383248236385
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val bothExpensesSuppliedRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99,
      |      "consolidatedExpenses": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costsOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99,
      |        "consolidatedExpenses": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val toDateBeforeFromDateRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2020-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "FRA",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costsOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)

  val ruleCountryCodeErrorRequestJson: JsValue = Json.parse(
    """
      |{
      |  "fromDate": "2018-04-06",
      |  "toDate": "2019-04-06",
      |  "foreignFhlEea": {
      |    "income": {
      |      "rentAmount": 5000.99
      |    },
      |    "expenditure": {
      |      "premisesRunningCosts": 5000.99,
      |      "repairsAndMaintenance": 5000.99,
      |      "financialCosts": 5000.99,
      |      "professionalFees": 5000.99,
      |      "costsOfServices": 5000.99,
      |      "travelCosts": 5000.99,
      |      "other": 5000.99
      |    }
      |  },
      |  "foreignProperty": [
      |    {
      |      "countryCode": "OFV",
      |      "income": {
      |        "rentIncome": {
      |          "rentAmount": 5000.99
      |        },
      |        "foreignTaxCreditRelief": false,
      |        "premiumOfLeaseGrant": 5000.99,
      |        "otherPropertyIncome": 5000.99,
      |        "foreignTaxTakenOff": 5000.99,
      |        "specialWithholdingTaxOrUKTaxPaid": 5000.99
      |      },
      |      "expenditure": {
      |        "premisesRunningCosts": 5000.99,
      |        "repairsAndMaintenance": 5000.99,
      |        "financialCosts": 5000.99,
      |        "professionalFees": 5000.99,
      |        "costsOfServices": 5000.99,
      |        "travelCosts": 5000.99,
      |        "residentialFinancialCost": 5000.99,
      |        "broughtFwdResidentialFinancialCost": 5000.99,
      |        "other": 5000.99
      |      }
      |    }
      |  ]
      |}
      |""".stripMargin)


  val allInvalidValueRequestError: MtdError = ValueFormatError.copy(
    message = "One or more monetary fields are invalid",
    paths = Some(List(
      "/foreignFhlEea/income/rentAmount",
      "/foreignFhlEea/expenditure/premisesRunningCosts",
      "/foreignFhlEea/expenditure/repairsAndMaintenance",
      "/foreignFhlEea/expenditure/financialCosts",
      "/foreignFhlEea/expenditure/professionalFees",
      "/foreignFhlEea/expenditure/costsOfServices",
      "/foreignFhlEea/expenditure/travelCosts",
      "/foreignFhlEea/expenditure/other",
      "/foreignProperty/0/income/rentIncome/rentAmount",
      "/foreignProperty/0/income/premiumOfLeaseGrant",
      "/foreignProperty/0/income/otherPropertyIncome",
      "/foreignProperty/0/income/foreignTaxTakenOff",
      "/foreignProperty/0/income/specialWithholdingTaxOrUKTaxPaid",
      "/foreignProperty/0/expenditure/premisesRunningCosts",
      "/foreignProperty/0/expenditure/repairsAndMaintenance",
      "/foreignProperty/0/expenditure/financialCosts",
      "/foreignProperty/0/expenditure/professionalFees",
      "/foreignProperty/0/expenditure/costsOfServices",
      "/foreignProperty/0/expenditure/travelCosts",
      "/foreignProperty/0/expenditure/residentialFinancialCost",
      "/foreignProperty/0/expenditure/broughtFwdResidentialFinancialCost",
      "/foreignProperty/0/expenditure/other"
    ))
  )

  val allInvalidCountryCodeRequestError: MtdError = CountryCodeFormatError.copy(
    paths = Some(List(
      "/foreignProperty/0/countryCode"
    ))
  )

  val allRuleCountryCodeRequestError: MtdError = RuleCountryCodeError.copy(
    paths = Some(List(
      "/foreignProperty/0/countryCode"
    ))
  )

  val RuleBothExpensesSuppliedRequestError: MtdError = RuleBothExpensesSuppliedError.copy(
    paths = Some(List(
      "/foreignFhlEea/expenditure",
      "/foreignProperty/0/expenditure"
    ))
  )


  private trait Test {
    val nino = "TC663795B"
    val businessId = "XAIS12345678910"


    def setupStubs(): StubMapping

    def uri: String

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    val responseBody = Json.parse(
      """
        |{
        |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        |  "links": [
        |    {
        |      "href":"/individuals/business/property/TC663795B/XAIS12345678910/period/4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
        |      "method":"GET",
        |      "rel":"self"
        |    }
        |  ]
        |}
        |""".stripMargin)

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin
  }

  "calling the create endpoint" should {

    trait CreateTest extends Test {
      def uri: String = s"/$nino/$businessId/period"

      def desUri: String = s"/income-tax/business/property/periodic/$nino/$businessId"

      val desResponse = Json.parse(
        """
          |{
          |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
          |}
          |  """.stripMargin)
    }

    "return a 201 status" when {

      "any valid unconsolidated request is made" in new CreateTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUri, Status.OK, desResponse)

        }

        val response: WSResponse = await(request().post(unconsolidatedRequestJson))
        response.status shouldBe Status.CREATED
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid consolidated request is made" in new CreateTest {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.POST, desUri, Status.OK, desResponse)

        }

        val response: WSResponse = await(request().post(consolidatedRequestBodyJson))
        response.status shouldBe Status.CREATED
        response.json shouldBe responseBody
        response.header("Content-Type") shouldBe Some("application/json")
      }


    }

    "return bad request error" when {
      "badly formed json body" in new CreateTest {
        private val json =
          s"""
             |{
             | badJson
             | }
             | """.stripMargin

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }
        val response: WSResponse = await(request().addHttpHeaders(("Content-Type", "application/json")).post(json))
        response.status shouldBe Status.BAD_REQUEST
        response.json shouldBe Json.toJson(BadRequestError)
      }
    }

    "return error according to spec" when {
      "validation error" when {
        def validationErrorTest(requestNino: String, requestBusinessId: String, requestBody: JsValue, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new CreateTest {

            override val nino: String = requestNino
            override val businessId: String = requestBusinessId

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

        val input = Seq(
          ("AA1123A", "XAIS12345678910", unconsolidatedRequestJson, Status.BAD_REQUEST, NinoFormatError),
          ("AA123456A", "XA***IS1", unconsolidatedRequestJson, Status.BAD_REQUEST, BusinessIdFormatError),
          ("AA123456A", "XAIS12345678910", invalidToDateRequestJson, Status.BAD_REQUEST, ToDateFormatError),
          ("AA123456A", "XAIS12345678910", invalidFromDateRequestJson, Status.BAD_REQUEST, FromDateFormatError),
          ("AA123456A", "XAIS12345678910", invalidCountryCodeRequestJson, Status.BAD_REQUEST, allInvalidCountryCodeRequestError),
          ("AA123456A", "XAIS12345678910", invalidValueRequestJson, Status.BAD_REQUEST, allInvalidValueRequestError),
          ("AA123456A", "XAIS12345678910", bothExpensesSuppliedRequestJson, Status.BAD_REQUEST, RuleBothExpensesSuppliedRequestError),
          ("AA123456A", "XAIS12345678910", toDateBeforeFromDateRequestJson, Status.BAD_REQUEST, RuleToDateBeforeFromDateError),
          ("AA123456A", "XAIS12345678910", ruleCountryCodeErrorRequestJson, Status.BAD_REQUEST, allRuleCountryCodeRequestError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }
      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new CreateTest {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.POST, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().post(unconsolidatedRequestJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INCOME_SOURCE_NOT_FOUND", Status.NOT_FOUND, NotFoundError),
          (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
          (Status.BAD_REQUEST, "INVALID_INCOMESOURCEID", Status.BAD_REQUEST, BusinessIdFormatError),
          (Status.BAD_REQUEST, "INVALID_PAYLOAD", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, DownstreamError),
          (Status.CONFLICT, "DUPLICATE_SUBMISSION", Status.BAD_REQUEST, RuleDuplicateSubmission),
          (Status.UNPROCESSABLE_ENTITY, "OVERLAPS_IN_PERIOD", Status.BAD_REQUEST, RuleOverlappingPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "NOT_ALIGN_PERIOD", Status.BAD_REQUEST, RuleMisalignedPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "GAPS_IN_PERIOD", Status.BAD_REQUEST, RuleNotContiguousPeriodError),
          (Status.UNPROCESSABLE_ENTITY, "INVALID_DATE_RANGE", Status.BAD_REQUEST, RuleToDateBeforeFromDateError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

}