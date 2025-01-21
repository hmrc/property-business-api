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

package v5.propertyPeriodSummary.list.model.response

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v5.propertyPeriodSummary.list.def1.model.response.SubmissionPeriod

class ListPropertyPeriodSummariesResponseSpec extends UnitSpec {

  "reads" should {
    "read from a single item array" in {
      val ifsJson = Json.parse(
        """
          |[
          |  {
          |    "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |    "fromDate": "2022-06-22",
          |    "toDate": "2023-06-22"
          |  }
          |]
        """.stripMargin
      )

      val expected = ListPropertyPeriodSummariesResponse(
        List(
          SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2022-06-22", "2023-06-22")
        ))

      val result = ifsJson.as[ListPropertyPeriodSummariesResponse]
      result shouldBe expected
    }

    "read from a multiple item array" in {
      val ifsJson = Json.parse(
        """
          |[
          |  {
          |    "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |    "fromDate": "2022-06-22",
          |    "toDate": "2022-06-22"
          |  },
          |  {
          |    "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3d",
          |    "fromDate": "2022-08-22",
          |    "toDate": "2022-08-22"
          |  }
          |]
        """.stripMargin
      )

      val expected = ListPropertyPeriodSummariesResponse(
        List(
          SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2022-06-22", "2022-06-22"),
          SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3d", "2022-08-22", "2022-08-22")
        ))

      val result = ifsJson.as[ListPropertyPeriodSummariesResponse]
      result shouldBe expected
    }

    "read an empty array" in {
      val ifsJson = Json.parse(
        """
          |[
          |
          |]
        """.stripMargin
      )

      val expected = ListPropertyPeriodSummariesResponse(List())

      val result = ifsJson.as[ListPropertyPeriodSummariesResponse]
      result shouldBe expected
    }
  }

  "writes" should {
    "write to JSON" in {
      val response = ListPropertyPeriodSummariesResponse(
        List(
          SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2022-06-22", "2022-06-22")
        ))

      val mtdJson = Json.parse(
        """
          |{
          |  "submissions": [
          |    {
          |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |      "fromDate": "2022-06-22",
          |      "toDate": "2022-06-22"
          |    }
          |  ]
          |}
        """.stripMargin
      )

      val result = Json.toJson(response)
      result shouldBe mtdJson
    }
  }

}
