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

package v4.retrieveUkPropertyAnnualSubmission.def2.model.response.def2_ukProperty

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Def2_Retrieve_UkPropertyBuilding(name: Option[String], number: Option[String], postcode: String)

object Def2_Retrieve_UkPropertyBuilding {
  implicit val writes: OWrites[Def2_Retrieve_UkPropertyBuilding] = Json.writes[Def2_Retrieve_UkPropertyBuilding]

  implicit val reads: Reads[Def2_Retrieve_UkPropertyBuilding] = (
    (__ \ "name").readNullable[String] and
      (__ \ "number").readNullable[String] and
      (__ \ "postCode").read[String]
  )(Def2_Retrieve_UkPropertyBuilding.apply _)

}
