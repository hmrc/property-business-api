/*
 * Copyright 2020 HM Revenue & Customs
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

package v2.models.response.retrieveUkPropertyPeriodSummary

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class NonFhlPropertyIncomeSpec extends UnitSpec {
  val downstreamJson: JsValue = Json.parse(
    """
      |{
      |  "premiumsOfLeaseGrant": 0,
      |  "reversePremiums": 0,
      |  "periodAmount": 0,
      |  "taxDeducted": 0,
      |  "otherIncome": 0,
      |  "ukOtherRentARoom": {
      |    "rentsReceived": 0
      |  }
      |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "premiumsOfLeaseGrant": 0,
      |  "reversePremiums": 0,
      |  "periodAmount": 0,
      |  "taxDeducted": 0,
      |  "otherIncome": 0,
      |  "rentARoom": {
      |    "rentsReceived": 0
      |  }
      |}
    """.stripMargin
  )

  val model: NonFhlPropertyIncome = NonFhlPropertyIncome(Some(0), Some(0), Some(0), Some(0), Some(0), Some(RentARoomIncome(Some(0))))

  "NonFhlPropertyIncome" when {
    "read from valid JSON" should {
      "return the expected model" in {
        downstreamJson.as[NonFhlPropertyIncome] shouldBe model
      }
    }

    "written JSON" should {
      "return the expected JSON" in {
        Json.toJson(model) shouldBe mtdJson
      }
    }
  }
}
