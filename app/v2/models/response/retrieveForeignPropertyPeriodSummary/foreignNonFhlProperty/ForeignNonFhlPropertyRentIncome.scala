/*
 * Copyright 2021 HM Revenue & Customs
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

package v2.models.response.retrieveForeignPropertyPeriodSummary.foreignNonFhlProperty

import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class ForeignNonFhlPropertyRentIncome(rentAmount: Option[BigDecimal])

object ForeignNonFhlPropertyRentIncome {
  implicit val writes: Writes[ForeignNonFhlPropertyRentIncome] = Json.writes[ForeignNonFhlPropertyRentIncome]
  implicit val reads: Reads[ForeignNonFhlPropertyRentIncome] =
    (JsPath \ "rentAmount").readNullable[BigDecimal].map(rentAmount => ForeignNonFhlPropertyRentIncome(rentAmount))
}