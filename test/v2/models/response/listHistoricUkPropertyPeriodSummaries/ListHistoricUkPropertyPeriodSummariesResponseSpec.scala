/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.models.response.listHistoricUkPropertyPeriodSummaries

import play.api.libs.json.Json
import support.UnitSpec

class ListHistoricUkPropertyPeriodSummariesResponseSpec extends UnitSpec {

  private val model = ListHistoricUkPropertyPeriodSummariesResponse(
    Seq(
      SubmissionPeriod(fromDate = "2020-01-02", toDate = "2020-03-04"),
      SubmissionPeriod(fromDate = "2021-01-02", toDate = "2021-03-04")
    ))

  "reads from downstream" should {
    "return the correct model" in {
      Json
        .parse(
          """{
          |  "annualAdjustments": [
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
        .as[ListHistoricUkPropertyPeriodSummariesResponse] shouldBe model
    }
  }

  "writes to MTD" should {
    "return a the correct JSON" in {
      Json.toJson(model) shouldBe Json.parse(
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
