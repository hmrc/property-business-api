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

import play.api.libs.json.{Json, Reads, Writes}

case class Income(rentAmount: BigDecimal, taxDeducted: Option[BigDecimal])

object Income {
  implicit val reads: Reads[Income] = Json.reads[Income]
  implicit val writes: Writes[Income] = Json.writes[Income]
}