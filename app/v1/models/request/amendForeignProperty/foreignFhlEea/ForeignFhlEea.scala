/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.request.amendForeignProperty.foreignFhlEea

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class ForeignFhlEea(income: Income, expenditure: Option[Expenditure])

object ForeignFhlEea {
  implicit val reads: Reads[ForeignFhlEea] = Json.reads[ForeignFhlEea]

  implicit val writes: Writes[ForeignFhlEea] = (
    (JsPath \ "income").write[Income] and
      (JsPath \ "expenses").writeNullable[Expenditure]
    ) (unlift(ForeignFhlEea.unapply))
}
