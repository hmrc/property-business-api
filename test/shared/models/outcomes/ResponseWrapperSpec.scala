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

package shared.models.outcomes

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class ResponseWrapperSpec extends UnitSpec {

  "ResponseWrapper" should {

    val responseData = Json.parse(
      """
        |{
        |   "who": "Knows"
        |}
    """.stripMargin
    )

    val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
    val wrapper       = ResponseWrapper(correlationId, responseData)

    "read in a singleError" in {
      val result: ResponseWrapper[JsValue] = wrapper.map(a => a)
      result shouldBe ResponseWrapper(correlationId, responseData)
    }
  }

}
