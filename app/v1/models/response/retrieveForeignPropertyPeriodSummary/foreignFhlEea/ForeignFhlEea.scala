/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.retrieveForeignPropertyPeriodSummary.foreignFhlEea

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

case class ForeignFhlEea(income: Option[ForeignFhlEeaIncome], expenditure: Option[ForeignFhlEeaExpenditure])

object ForeignFhlEea {
  implicit val writes: Writes[ForeignFhlEea] = Json.writes[ForeignFhlEea]
  implicit val reads: Reads[ForeignFhlEea] = (
    (JsPath \ "income").readNullable[ForeignFhlEeaIncome] and
      (JsPath \ "expenses").readNullable[ForeignFhlEeaExpenditure]
    )(ForeignFhlEea.apply _)
}
