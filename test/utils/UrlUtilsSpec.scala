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

package utils

import support.UnitSpec
import utils.UrlUtils.appendQueryParams

class UrlUtilsSpec extends UnitSpec {

  "appendQueryParams" should {
    "given an empty queryParams list, return an unchanged URL" in {
      val url         = "http://something/else"
      val queryParams = List()
      val result      = appendQueryParams(url, queryParams)
      result shouldBe url
    }

    "given a URL with no query params of its own, return a URL with added queryParams" in {
      val url         = "http://something/else"
      val queryParams = List("taxYear" -> "23-24")
      val result      = appendQueryParams(url, queryParams)
      result shouldBe "http://something/else?taxYear=23-24"
    }

    "given a URL with query params, return a URL with no extra queryParams" in {
      val url         = "http://something/else?alreadyGot=this"
      val queryParams = List()
      val result      = appendQueryParams(url, queryParams)
      result shouldBe "http://something/else?alreadyGot=this"
    }

    "given a URL with query params, return a URL with added queryParams" in {
      val url         = "http://something/else?alreadyGot=this"
      val queryParams = List("taxYear" -> "23-24")
      val result      = appendQueryParams(url, queryParams)
      result shouldBe "http://something/else?alreadyGot=this&taxYear=23-24"
    }

  }
}
