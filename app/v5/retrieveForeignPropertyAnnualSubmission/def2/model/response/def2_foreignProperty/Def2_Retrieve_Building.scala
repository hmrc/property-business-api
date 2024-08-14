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

package v5.retrieveForeignPropertyAnnualSubmission.def2.model.response.def2_foreignProperty

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Def2_Retrieve_Building(name: Option[String], number: Option[String], postcode: String)

object Def2_Retrieve_Building {
  implicit val writes: OWrites[Def2_Retrieve_Building] = Json.writes[Def2_Retrieve_Building]

  implicit val reads: Reads[Def2_Retrieve_Building] = (
    (JsPath \ "name").readNullable[String] and
      (JsPath \ "number").readNullable[String] and
      (JsPath \ "postCode").read[String]
  )(Def2_Retrieve_Building.apply _)

}
