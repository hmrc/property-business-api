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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.models.response.listForeignPropertiesPeriodSummaries

import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.hateoas.Link
import v1.models.hateoas.Method.{GET, POST}

class ListForeignPropertiesPeriodSummariesResponseSpec extends UnitSpec with MockAppConfig {
  "reads" should {
    "read from a single item array" in {
      val desJson = Json.parse(
        """
          |[
          |  {
          |    "submittedOn": "2020-06-22T22:00:20Z",
          |    "foreignFhlEea": {
          |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |      "fromDate": "2020-06-22",
          |      "toDate": "2020-06-22"
          |    },
          |    "foreignProperty": {
          |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |      "fromDate": "2020-06-22",
          |      "toDate": "2020-06-22"
          |    }
          |  }
          |]
          |""".stripMargin)

      val model = ListForeignPropertiesPeriodSummariesResponse(Seq(
        SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")
      ))

      desJson.as[ListForeignPropertiesPeriodSummariesResponse[SubmissionPeriod]] shouldBe model
    }
    "read from a multiple item array" in {
      val desJson = Json.parse(
        """
          |[
          |  {
          |    "submittedOn": "2020-06-22T22:00:20Z",
          |    "foreignProperty": {
          |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |      "fromDate": "2020-06-22",
          |      "toDate": "2020-06-22"
          |    },
          |    "foreignFhlEea": {
          |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |      "fromDate": "2020-06-22",
          |      "toDate": "2020-06-22"
          |    }
          |  },
          |  {
          |    "submittedOn": "2020-08-22T22:00:20Z",
          |    "foreignFhlEea": {
          |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3d",
          |      "fromDate": "2020-08-22",
          |      "toDate": "2020-08-22"
          |    }
          |  }
          |]
          |""".stripMargin)

      val model = ListForeignPropertiesPeriodSummariesResponse(Seq(
        SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22"),
        SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3d", "2020-08-22", "2020-08-22")
      ))

      desJson.as[ListForeignPropertiesPeriodSummariesResponse[SubmissionPeriod]] shouldBe model
    }
    "read an empty array" in {
      val desJson = Json.parse(
        """
          |[
          |
          |]
          |""".stripMargin)

      val model = ListForeignPropertiesPeriodSummariesResponse(Seq())

      desJson.as[ListForeignPropertiesPeriodSummariesResponse[SubmissionPeriod]] shouldBe model
    }
  }

  "writes" should {
    "write to JSON" in {
      val model = ListForeignPropertiesPeriodSummariesResponse(Seq(
        SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")
      ))

      val mtdJson = Json.parse(
        """
          |{
          |  "submissions": [
          |    {
          |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |      "fromDate": "2020-06-22",
          |      "toDate": "2020-06-22"
          |    }
          |  ]
          |}
          |""".stripMargin)

      Json.toJson(model) shouldBe mtdJson
    }
  }

  "Links Factory" should {
    val nino = "mynino"
    val businessId = "mysubmissionid"
    val submissionId = "mysubmissionid"

    "expose the correct top level links for list" in {
      MockedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes
      ListForeignPropertiesPeriodSummariesResponse.LinksFactory.links(mockAppConfig, ListForeignPropertiesPeriodSummariesHateoasData(nino, businessId)) shouldBe
        Seq(
          Link(s"/my/context/$nino/$businessId/period", GET, "self"),
          Link(s"/my/context/$nino/$businessId/period", POST, "create-property-period-summary")
        )
    }

    "expose the correct item level links for list" in {
      MockedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes
      ListForeignPropertiesPeriodSummariesResponse.LinksFactory.itemLinks(mockAppConfig, ListForeignPropertiesPeriodSummariesHateoasData(nino, businessId),
        SubmissionPeriod(submissionId, "", "")) shouldBe
        Seq(
          Link(s"/my/context/$nino/$businessId/period/$submissionId", GET, "self")
        )
    }
  }

  "Response Functor" should {
    "apply the map function" in {
      ListForeignPropertiesPeriodSummariesResponse.ResponseFunctor.map(ListForeignPropertiesPeriodSummariesResponse(Seq(1)))(_.toString) shouldBe ListForeignPropertiesPeriodSummariesResponse(Seq("1"))
    }
  }
}
