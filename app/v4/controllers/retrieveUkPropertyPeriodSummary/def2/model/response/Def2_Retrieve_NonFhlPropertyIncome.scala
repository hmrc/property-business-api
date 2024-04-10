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

package v4.controllers.retrieveUkPropertyPeriodSummary.def2.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Def2_Retrieve_NonFhlPropertyIncome(premiumsOfLeaseGrant: Option[BigDecimal],
                                              reversePremiums: Option[BigDecimal],
                                              periodAmount: Option[BigDecimal],
                                              taxDeducted: Option[BigDecimal],
                                              otherIncome: Option[BigDecimal],
                                              rentARoom: Option[Def2_Retrieve_RentARoomIncome])

object Def2_Retrieve_NonFhlPropertyIncome {
  implicit val writes: OWrites[Def2_Retrieve_NonFhlPropertyIncome] = Json.writes[Def2_Retrieve_NonFhlPropertyIncome]

  implicit val reads: Reads[Def2_Retrieve_NonFhlPropertyIncome] = (
    (JsPath \ "premiumsOfLeaseGrant").readNullable[BigDecimal] and
      (JsPath \ "reversePremiums").readNullable[BigDecimal] and
      (JsPath \ "periodAmount").readNullable[BigDecimal] and
      (JsPath \ "taxDeducted").readNullable[BigDecimal] and
      (JsPath \ "otherIncome").readNullable[BigDecimal] and
      (JsPath \ "ukOtherRentARoom").readNullable[Def2_Retrieve_RentARoomIncome]
  )(Def2_Retrieve_NonFhlPropertyIncome.apply _)

}
