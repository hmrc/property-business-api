/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsSuccess, Json, Reads}
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import support.UnitSpec

class JsonReadsUtilsSpec extends UnitSpec {

  case class testModel(value: Option[BigDecimal], value2: Option[BigDecimal], value3: Option[BigDecimal])

  object testModel {

    implicit val reads: Reads[testModel] = (
      JsonReadsUtils.readValidOption((JsPath \ "value1A").readNullable[BigDecimal], (JsPath \ "value1B").readNullable[BigDecimal]) and
        JsonReadsUtils.readValidOption((JsPath \ "value2A").readNullable[BigDecimal], (JsPath \ "value2B").readNullable[BigDecimal]) and
        JsonReadsUtils.readValidOption((JsPath \ "value3A").readNullable[BigDecimal], (JsPath \ "value3B").readNullable[BigDecimal])
    )(testModel.apply _)

  }

  private val testJson = Json.parse("""
      |{
      |    "value1A": 500.00,
      |    "value2B": 600.00
      |}
      |""".stripMargin)

  "JsonReadsUtilsSpec" when {
    "readValidOption" should {
      "return the first reads" in {
        lazy val fakeRequest: FakeRequest[AnyContentAsJson] = FakeRequest().withJsonBody(testJson)
        val result                                          = fakeRequest.body.json.validate(testModel.reads)
        result shouldBe JsSuccess[testModel](testModel(Some(500.00), Some(600.00), None))
      }
    }
  }

}
