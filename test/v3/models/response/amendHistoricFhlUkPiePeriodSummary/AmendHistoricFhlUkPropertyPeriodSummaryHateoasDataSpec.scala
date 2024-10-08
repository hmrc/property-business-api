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

package v3.models.response.amendHistoricFhlUkPiePeriodSummary

import api.hateoas.Link
import api.hateoas.Method._
import config.MockAppConfig
import support.UnitSpec

class AmendHistoricFhlUkPropertyPeriodSummaryHateoasDataSpec extends UnitSpec with MockAppConfig {

  "LinksFactory" should {
    "return the correct links" in {
      val nino     = "someNino"
      val periodId = "somePeriodId"
      val context  = "some/context"

      MockedAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes()

      AmendHistoricFhlUkPropertyPeriodSummaryHateoasData.LinksFactory
        .links(mockAppConfig, AmendHistoricFhlUkPropertyPeriodSummaryHateoasData(nino, periodId)) shouldBe
        List(
          Link(s"/$context/uk/period/furnished-holiday-lettings/$nino/$periodId", PUT, "amend-uk-property-historic-fhl-period-summary"),
          Link(s"/$context/uk/period/furnished-holiday-lettings/$nino/$periodId", GET, "self"),
          Link(s"/$context/uk/period/furnished-holiday-lettings/$nino", GET, "list-uk-property-historic-fhl-period-summaries")
        )
    }

  }

}
