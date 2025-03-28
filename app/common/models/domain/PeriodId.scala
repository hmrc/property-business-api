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

package common.models.domain

import play.api.libs.json.{JsString, Writes}
import shared.models.domain.DateRange

import java.time.format.DateTimeFormatter

case class PeriodId(value: String) {

  val (from, to): (String, String) = {
    if (value.length == 21) {
      val f = value.substring(0, 10)
      val t = value.substring(11, 21)
      (f, t)
    } else {
      ("", "")
    }
  }

}

object PeriodId {
  implicit val writes: Writes[PeriodId] = Writes(x => JsString(x.value))

  def apply(dateRange: DateRange): PeriodId = {
    PeriodId(
      dateRange.startDate.format(DateTimeFormatter.ISO_DATE),
      dateRange.endDate.format(DateTimeFormatter.ISO_DATE)
    )
  }

  def apply(from: String, to: String): PeriodId = {
    PeriodId(s"${from}_$to")
  }

}
