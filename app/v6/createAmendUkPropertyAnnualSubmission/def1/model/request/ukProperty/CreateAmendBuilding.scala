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

package v6.createAmendUkPropertyAnnualSubmission.def1.model.request.ukProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class CreateAmendBuilding(name: Option[String], number: Option[String], postcode: String)

object CreateAmendBuilding {
  implicit val reads: Reads[CreateAmendBuilding] = Json.reads[CreateAmendBuilding]

  implicit val writes: Writes[CreateAmendBuilding] = (
    (JsPath \ "name").writeNullable[String] and
      (JsPath \ "number").writeNullable[String] and
      (JsPath \ "postCode").write[String]
  )(unlift(CreateAmendBuilding.unapply))

}
