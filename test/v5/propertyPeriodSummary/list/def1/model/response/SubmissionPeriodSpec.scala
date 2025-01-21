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

package v5.propertyPeriodSummary.list.def1.model.response

import play.api.libs.json.Json
import shared.utils.UnitSpec

class SubmissionPeriodSpec extends UnitSpec {

  "reads" should {
    "return a valid model" when {
      "a valid json is supplied" in {
        val ifsJson = Json.parse(
          """
            |{
            |   "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            |   "fromDate": "2022-06-22",
            |   "toDate": "2023-06-22"
            |}
        """.stripMargin
        )

        val model = SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2022-06-22", "2023-06-22")

        ifsJson.as[SubmissionPeriod] shouldBe model
      }
    }
  }

  "writes" should {
    "return a valid JSON" when {
      "a valid model is supplied" in {
        val model = SubmissionPeriod("4557ecb5-fd32-48cc-81f5-e6acd1099f3c", "2022-06-22", "2023-06-22")

        val mtdJson = Json.parse(
          """
            |{
            |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c",
            |  "fromDate": "2022-06-22",
            |  "toDate": "2023-06-22"
            |}
      """.stripMargin
        )

        Json.toJson(model) shouldBe mtdJson
      }
    }
  }

}
