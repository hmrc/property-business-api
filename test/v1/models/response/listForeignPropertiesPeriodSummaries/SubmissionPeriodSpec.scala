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

import play.api.libs.json.Json
import support.UnitSpec

class SubmissionPeriodSpec extends UnitSpec {
  "reads" should {
    "read when only foreignFhlEea is provided" in {
      val desJson = Json.parse(
        """
          |{
          |    "submittedOn": "2020-06-22T22:00:20Z",
          |    "foreignFhlEea": {
          |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |      "fromDate": "2020-06-22",
          |      "toDate": "2020-06-22"
          |    }
          |  }
          |""".stripMargin)

      val model = SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")

      desJson.as[SubmissionPeriod] shouldBe model
    }
    "read when only foreignProperty is provided" in {
      val desJson = Json.parse(
        """
          |{
          |    "submittedOn": "2020-06-22T22:00:20Z",
          |    "foreignProperty": {
          |      "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |      "fromDate": "2020-06-22",
          |      "toDate": "2020-06-22"
          |    }
          |  }
          |""".stripMargin)

      val model = SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")

      desJson.as[SubmissionPeriod] shouldBe model
    }
    "read when foreignProperty is provided before foreignFhlEea" in {
      val desJson = Json.parse(
        """
          |{
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
          |  }
          |""".stripMargin)

      val model = SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")

      desJson.as[SubmissionPeriod] shouldBe model
    }
    "read when foreignFhlEea is provided before foreignProperty" in {
      val desJson = Json.parse(
        """
          |{
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
          |""".stripMargin)

      val model = SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")

      desJson.as[SubmissionPeriod] shouldBe model
    }
  }

  "writes" should {
    "write to JSON" in {
      val model = SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2020-06-22", "2020-06-22")

      val mtdJson = Json.parse(
        """
          |{
          |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
          |  "fromDate": "2020-06-22",
          |  "toDate": "2020-06-22"
          |}
          |""".stripMargin)

      Json.toJson(model) shouldBe mtdJson
    }
  }
}
