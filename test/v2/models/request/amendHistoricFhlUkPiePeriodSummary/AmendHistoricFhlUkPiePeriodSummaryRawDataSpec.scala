/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.models.request.amendHistoricFhlUkPiePeriodSummary

import play.api.libs.json.Json
import support.UnitSpec

class AmendHistoricFhlUkPiePeriodSummaryRawDataSpec extends UnitSpec {
  "writes" must {
    "work" in {
      Json.toJson(AmendHistoricFhlUkPiePeriodSummaryRawData(nino = "NS123456A",
                                                            periodId = "2022-01-01_2022-01-02",
                                                            body = Json.obj("body" -> "value"))) shouldBe
        Json.parse(
          """
          |{
          |"nino": "NS123456A",
          |"periodId": "2022-01-01_2022-01-02",
          | "request": {
          | "body": "value"
          | }
          |}
          |""".stripMargin
        )
    }
  }

}
