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

package v3.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v3.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino: String       = "AA123456A"
    val taxYear: String    = "2022-23"
    val businessId: String = "XAIS12345678910"

    def uri: String = s"/$nino/$businessId/annual/$taxYear"

    def ifsUri: String = s"/income-tax/business/property/annual"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def ifsQueryParams: Map[String, String] = Map(
      "taxableEntityId" -> nino,
      "incomeSourceId"  -> businessId,
      "taxYear"         -> taxYear
    )

  }

  "calling the delete property annual submission endpoint" when {

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {

      "MTD ID lookup fails with a 403" should {

        "return 403" in new Test {
          override val nino: String = "AA123456A"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            MtdIdLookupStub.error(nino, Status.FORBIDDEN)
          }

          val response: WSResponse = await(request().delete())
          response.status shouldBe Status.FORBIDDEN
        }

      }

      "return success status" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.DELETE, ifsUri, ifsQueryParams, Status.NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe Status.NO_CONTENT
      }
    }

    "the NINO cannot be converted to a MTD ID" should {

      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.error(nino, Status.INTERNAL_SERVER_ERROR)
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT logged in" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe Status.FORBIDDEN
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT authorised" should {

      "return 403" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().delete())
        response.status shouldBe Status.FORBIDDEN
      }
    }
  }

}
