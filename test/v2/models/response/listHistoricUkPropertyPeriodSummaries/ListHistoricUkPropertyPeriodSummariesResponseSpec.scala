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

package v2.models.response.listHistoricUkPropertyPeriodSummaries

import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v2.models.domain.{ HistoricPropertyType, PeriodId }
import v2.models.hateoas.Link
import v2.models.hateoas.Method._

class ListHistoricUkPropertyPeriodSummariesResponseSpec extends UnitSpec with MockAppConfig {

  private val model = ListHistoricUkPropertyPeriodSummariesResponse(
    Seq(
      SubmissionPeriod(fromDate = "2020-01-02", toDate = "2020-03-04"),
      SubmissionPeriod(fromDate = "2021-01-02", toDate = "2021-03-04")
    ))

  "reads from downstream" should {
    "handle empty model" in {
      Json
        .parse(
          """{
          |  "periods": []
          |}
          """.stripMargin
        )
        .as[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]] shouldBe ListHistoricUkPropertyPeriodSummariesResponse(Nil)
    }

    "return the correct model" in {
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
        .as[ListHistoricUkPropertyPeriodSummariesResponse[SubmissionPeriod]] shouldBe model
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

  "LinksFactory" when {
    val nino     = "someNino"
    val context  = "some/context"
    val from     = "2020-01-02"
    val to       = "2020-03-04"
    val periodId = PeriodId(from, to)

    val linksFactory = ListHistoricUkPropertyPeriodSummariesResponse.LinksFactory

    "fhl" must {
      val data: ListHistoricUkPropertyPeriodSummariesHateoasData =
        ListHistoricUkPropertyPeriodSummariesHateoasData(nino, HistoricPropertyType.Fhl)

      "produce the correct links" in {
        MockAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes()

        linksFactory.links(mockAppConfig, data) shouldBe
          Seq(
            Link(s"/$context/uk/period/furnished-holiday-lettings/$nino", GET, "self"),
            Link(s"/$context/uk/period/furnished-holiday-lettings/$nino", POST, "create-uk-property-historic-fhl-period-summary")
          )
      }

      "produce the correct item links" in {
        MockAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes()

        val item = SubmissionPeriod(from, to)

        linksFactory.itemLinks(mockAppConfig, data, item) shouldBe
          Seq(
            Link(s"/$context/uk/period/furnished-holiday-lettings/$nino/${periodId.value}", PUT, "amend-uk-property-historic-fhl-period-summary"),
            Link(s"/$context/uk/period/furnished-holiday-lettings/$nino/${periodId.value}", GET, "self")
          )
      }
    }

    "non-fhl" must {
      val data: ListHistoricUkPropertyPeriodSummariesHateoasData =
        ListHistoricUkPropertyPeriodSummariesHateoasData(nino, HistoricPropertyType.NonFhl)

      "produce the correct links" in {
        MockAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes()

        linksFactory.links(mockAppConfig, data) shouldBe
          Seq(
            Link(s"/$context/uk/period/non-furnished-holiday-lettings/$nino", GET, "self"),
            Link(s"/$context/uk/period/non-furnished-holiday-lettings/$nino", POST, "create-uk-property-historic-non-fhl-period-summary")
          )
      }

      "produce the correct item links" in {
        MockAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes()

        val item = SubmissionPeriod(from, to)

        linksFactory.itemLinks(mockAppConfig, data, item) shouldBe
          Seq(
            Link(s"/$context/uk/period/non-furnished-holiday-lettings/$nino/${periodId.value}",
                 PUT,
                 "amend-uk-property-historic-non-fhl-period-summary"),
            Link(s"/$context/uk/period/non-furnished-holiday-lettings/$nino/${periodId.value}", GET, "self")
          )
      }
    }
  }
}
