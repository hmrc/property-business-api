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

package v6.createForeignPropertyDetails.def1.model.request

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*
import v6.createForeignPropertyDetails.model.request.CreateForeignPropertyDetailsRequestBody

case class Def1_CreateForeignPropertyDetailsRequestBody(propertyName: String, countryCode: String, endDate: Option[String], endReason: Option[String])
    extends CreateForeignPropertyDetailsRequestBody

object Def1_CreateForeignPropertyDetailsRequestBody {

  implicit val reads: Reads[Def1_CreateForeignPropertyDetailsRequestBody] = Json.reads[Def1_CreateForeignPropertyDetailsRequestBody]

  implicit val writes: OWrites[Def1_CreateForeignPropertyDetailsRequestBody] = (
    (JsPath \ "foreignPropertyDetails" \ "propertyName").write[String] and
      (JsPath \ "foreignPropertyDetails" \ "countryCode").write[String] and
      (JsPath \ "foreignPropertyDetails" \ "endDate").writeNullable[String] and
      (JsPath \ "foreignPropertyDetails" \ "endReason").writeNullable[String].contramap[Option[String]](_.map(EndReason.parser(_).toDownstream))
  )(o => Tuple.fromProductTyped(o))

}
