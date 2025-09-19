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

package v5.historicNonFhlUkPropertyPeriodSummary.list.model.response

import play.api.libs.json.*
import v5.historicNonFhlUkPropertyPeriodSummary.list.def1.model.response.SubmissionPeriod

sealed trait ListHistoricNonFhlUkPropertyPeriodSummariesResponse {
  val submissions: Seq[SubmissionPeriod]
}

object ListHistoricNonFhlUkPropertyPeriodSummariesResponse {

  implicit val writes: OWrites[ListHistoricNonFhlUkPropertyPeriodSummariesResponse] = {
    case def1: Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse =>
      Json.toJsObject(def1)
  }

}

case class Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse(
    submissions: Seq[SubmissionPeriod]
) extends ListHistoricNonFhlUkPropertyPeriodSummariesResponse

object Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse {

  implicit val reads: Reads[Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse] =
    (__ \ "periods").read[List[SubmissionPeriod]].map(Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse(_))

  implicit val writes: OWrites[Def1_ListHistoricNonFhlUkPropertyPeriodSummariesResponse] = Json.writes
}
