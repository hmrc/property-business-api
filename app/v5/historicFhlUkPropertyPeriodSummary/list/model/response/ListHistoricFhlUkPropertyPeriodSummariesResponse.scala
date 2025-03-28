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

package v5.historicFhlUkPropertyPeriodSummary.list.model.response

import cats.Functor
import play.api.libs.json._

case class ListHistoricFhlUkPropertyPeriodSummariesResponse[I](submissions: Seq[I])

object ListHistoricFhlUkPropertyPeriodSummariesResponse {

  implicit def reads[I: Reads]: Reads[ListHistoricFhlUkPropertyPeriodSummariesResponse[I]] =
    (__ \ "periods").read[List[I]].map(ListHistoricFhlUkPropertyPeriodSummariesResponse(_))

  implicit def writes[I: Writes]: OWrites[ListHistoricFhlUkPropertyPeriodSummariesResponse[I]] = Json.writes

  implicit object ResponseFunctor extends Functor[ListHistoricFhlUkPropertyPeriodSummariesResponse] {

    override def map[A, B](fa: ListHistoricFhlUkPropertyPeriodSummariesResponse[A])(f: A => B): ListHistoricFhlUkPropertyPeriodSummariesResponse[B] =
      ListHistoricFhlUkPropertyPeriodSummariesResponse(fa.submissions.map(f))

  }

}
