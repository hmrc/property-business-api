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

package v6.updateForeignPropertyDetails.def1.model.request

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v6.updateForeignPropertyDetails.model.request.UpdateForeignPropertyDetailsRequestBody

case class Def1_UpdateForeignPropertyDetailsRequestBody(propertyName: String, endDate: Option[String], endReason: Option[String])
    extends UpdateForeignPropertyDetailsRequestBody

object Def1_UpdateForeignPropertyDetailsRequestBody {

  implicit val reads: Reads[Def1_UpdateForeignPropertyDetailsRequestBody] = Json.reads[Def1_UpdateForeignPropertyDetailsRequestBody]

  implicit val writes: OWrites[Def1_UpdateForeignPropertyDetailsRequestBody] = (
    (JsPath \ "foreignPropertyDetails" \ "propertyName").write[String] and
      (JsPath \ "foreignPropertyDetails" \ "endDate").writeNullable[String] and
      (JsPath \ "foreignPropertyDetails" \ "endReason").writeNullable[String].contramap[Option[String]](_.map(EndReason.parser(_).toDownstream))
  )(o => Tuple.fromProductTyped(o))

}
