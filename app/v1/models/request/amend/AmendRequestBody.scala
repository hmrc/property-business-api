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

package v1.models.request.amend

import play.api.libs.json.{Json, Reads, Writes}
import v1.models.request.amend.foreignFhlEea.ForeignFhlEea
import v1.models.request.amend.foreignPropertyEntry.ForeignPropertyEntry

case class AmendRequestBody(foreignFhlEea: Option[ForeignFhlEea], foreignProperty: Option[Seq[ForeignPropertyEntry]]) {
  def isEmpty: Boolean = (foreignFhlEea.isEmpty && foreignProperty.isEmpty) ||
    foreignFhlEea.flatMap(_.expenditure.map(_.isEmpty)).getOrElse(false) ||
    foreignProperty.exists(_.isEmpty) ||
    foreignProperty.exists(_.exists(_.expenditure.exists(_.isEmpty)))
}

object AmendRequestBody {
  implicit val reads: Reads[AmendRequestBody] = Json.reads[AmendRequestBody]
  implicit val writes: Writes[AmendRequestBody] = Json.writes[AmendRequestBody]
}


