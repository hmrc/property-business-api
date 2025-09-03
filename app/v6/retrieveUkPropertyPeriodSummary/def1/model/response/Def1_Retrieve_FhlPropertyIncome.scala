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

package v6.retrieveUkPropertyPeriodSummary.def1.model.response

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Def1_Retrieve_FhlPropertyIncome(periodAmount: Option[BigDecimal],
                                           taxDeducted: Option[BigDecimal],
                                           rentARoom: Option[Def1_Retrieve_RentARoomIncome])

object Def1_Retrieve_FhlPropertyIncome {
  implicit val writes: OWrites[Def1_Retrieve_FhlPropertyIncome] = Json.writes[Def1_Retrieve_FhlPropertyIncome]

  implicit val reads: Reads[Def1_Retrieve_FhlPropertyIncome] = (
    (JsPath \ "periodAmount").readNullable[BigDecimal] and
      (JsPath \ "taxDeducted").readNullable[BigDecimal] and
      (JsPath \ "ukFhlRentARoom").readNullable[Def1_Retrieve_RentARoomIncome]
  )(Def1_Retrieve_FhlPropertyIncome.apply _)

}
