/*
 * Copyright 2025 HM Revenue & Customs
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

package v6.retrieveForeignPropertyDetails.def1.model.response

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, OWrites, Reads, __}
import v6.retrieveForeignPropertyDetails.model.response.RetrieveForeignPropertyDetailsResponse

case class Def1_RetrieveForeignPropertyDetailsResponse(
    foreignPropertyDetails: Seq[ForeignPropertyDetailsEntry]
) extends RetrieveForeignPropertyDetailsResponse {}

object Def1_RetrieveForeignPropertyDetailsResponse {

  implicit val writes: OWrites[Def1_RetrieveForeignPropertyDetailsResponse] =
    Json.writes[Def1_RetrieveForeignPropertyDetailsResponse]

  implicit val reads: Reads[Def1_RetrieveForeignPropertyDetailsResponse] = (
    (__ \ "foreignPropertyDetails").readNullable[Seq[ForeignPropertyDetailsEntry]]
  )(Def1_RetrieveForeignPropertyDetailsResponse.apply)

}
