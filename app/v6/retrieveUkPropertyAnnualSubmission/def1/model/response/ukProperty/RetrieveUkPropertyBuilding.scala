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

package v6.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class RetrieveUkPropertyBuilding(name: Option[String], number: Option[String], postcode: String)

object RetrieveUkPropertyBuilding {
  implicit val writes: OWrites[RetrieveUkPropertyBuilding] = Json.writes[RetrieveUkPropertyBuilding]

  implicit val reads: Reads[RetrieveUkPropertyBuilding] = (
    (__ \ "name").readNullable[String] and
      (__ \ "number").readNullable[String] and
      (__ \ "postCode").read[String]
  )(RetrieveUkPropertyBuilding.apply _)

}
