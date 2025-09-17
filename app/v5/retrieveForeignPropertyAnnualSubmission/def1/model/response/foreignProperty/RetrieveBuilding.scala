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

package v5.retrieveForeignPropertyAnnualSubmission.def1.model.response.foreignProperty

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveBuilding(name: Option[String], number: Option[String], postcode: String)

object RetrieveBuilding {
  implicit val writes: OWrites[RetrieveBuilding] = Json.writes[RetrieveBuilding]

  implicit val reads: Reads[RetrieveBuilding] = (
    (JsPath \ "name").readNullable[String] and
      (JsPath \ "number").readNullable[String] and
      (JsPath \ "postCode").read[String]
  )(RetrieveBuilding.apply)

}
