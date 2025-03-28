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

package v6.historicFhlUkPropertyPeriodSummary.retrieve.def1.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class PeriodIncome(periodAmount: Option[BigDecimal], taxDeducted: Option[BigDecimal], rentARoom: Option[RentARoomIncome])

case object PeriodIncome {
  implicit val writes: OWrites[PeriodIncome] = Json.writes[PeriodIncome]

  implicit val reads: Reads[PeriodIncome] = (
    (JsPath \ "rentIncome" \ "amount").readNullable[BigDecimal] and
      (JsPath \ "rentIncome" \ "taxDeducted").readNullable[BigDecimal] and
      (JsPath \ "ukRentARoom").readNullable[RentARoomIncome]
  )(PeriodIncome.apply _)

}
