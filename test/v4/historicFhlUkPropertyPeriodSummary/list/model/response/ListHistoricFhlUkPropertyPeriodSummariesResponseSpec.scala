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

package v4.historicFhlUkPropertyPeriodSummary.list.model.response

import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v4.historicFhlUkPropertyPeriodSummary.list.def1.model.response.SubmissionPeriod

class ListHistoricFhlUkPropertyPeriodSummariesResponseSpec extends UnitSpec with MockAppConfig {

  private val parsedResponse = ListHistoricFhlUkPropertyPeriodSummariesResponse(
    List(
      SubmissionPeriod(fromDate = "2020-01-02", toDate = "2020-03-04"),
      SubmissionPeriod(fromDate = "2021-01-02", toDate = "2021-03-04")
    ))

  "reads from downstream" should {
    "handle minimal valid JSON with an empty periods array" in {
      val result = Json
        .parse(
          """{
            |  "periods": []
            |}
          """.stripMargin
        )
        .as[ListHistoricFhlUkPropertyPeriodSummariesResponse[SubmissionPeriod]]

      result shouldBe ListHistoricFhlUkPropertyPeriodSummariesResponse(Nil)
    }

    "return the parsed object" in {
      Json
        .parse(
          """{
          |  "periods": [
          |    {
          |      "transactionReference": "ignored",
          |      "from": "2020-01-02",
          |      "to": "2020-03-04"
          |    },
          |    {
          |      "transactionReference": "ignored",
          |      "from": "2021-01-02",
          |      "to": "2021-03-04"
          |    }
          |  ]
          |}
          """.stripMargin
        )
        .as[ListHistoricFhlUkPropertyPeriodSummariesResponse[SubmissionPeriod]] shouldBe parsedResponse
    }
  }

  "writes to MTD" should {
    "return a the correct JSON" in {
      val result = Json.toJson(parsedResponse)

      result shouldBe Json.parse(
        """{
          |  "submissions": [
          |    {
          |      "fromDate": "2020-01-02",
          |      "toDate": "2020-03-04",
          |      "periodId": "2020-01-02_2020-03-04"
          |    },
          |    {
          |      "fromDate": "2021-01-02",
          |      "toDate": "2021-03-04",
          |      "periodId": "2021-01-02_2021-03-04"
          |    }
          |  ]
          |}
        """.stripMargin
      )
    }
  }

}
