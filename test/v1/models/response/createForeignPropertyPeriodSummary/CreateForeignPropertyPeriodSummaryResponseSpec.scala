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

package v1.models.response.createForeignPropertyPeriodSummary

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class CreateForeignPropertyPeriodSummaryResponseSpec extends UnitSpec with JsonErrorValidators {

  val createForeignPropertyResponse = CreateForeignPropertyPeriodSummaryResponse(
    "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  )

  val json = Json.parse(
    """{
      |  "submissionId": "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
      |}""".stripMargin)

  "reads" when {
    "passed a valid JSON" should {
      "return a valid model" in {
        json.as[CreateForeignPropertyPeriodSummaryResponse] shouldBe createForeignPropertyResponse
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(createForeignPropertyResponse) shouldBe json
      }
    }
  }
}
