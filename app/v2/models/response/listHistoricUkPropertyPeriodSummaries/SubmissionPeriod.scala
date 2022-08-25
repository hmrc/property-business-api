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

package v2.models.response.listHistoricUkPropertyPeriodSummaries

import play.api.libs.functional.syntax._
import play.api.libs.json._
import v2.models.domain.PeriodId

case class SubmissionPeriod(fromDate: String, toDate: String) {
  def periodId: PeriodId = PeriodId(fromDate, toDate)
}

object SubmissionPeriod {
  implicit val reads: Reads[SubmissionPeriod] = (
    (__ \ "from").read[String] and
      (__ \ "to").read[String]
  )(SubmissionPeriod.apply _)

  implicit val writes: Writes[SubmissionPeriod] = Writes { x =>
    Json.writes[SubmissionPeriod].writes(x) ++ Json.obj("periodId" -> x.periodId)
  }

}
