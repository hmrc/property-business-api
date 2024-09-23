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

package v5.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class RetrieveUkProperty(adjustments: Option[RetrieveUkPropertyAdjustments], allowances: Option[RetrieveUkPropertyAllowances])

object RetrieveUkProperty {
  implicit val writes: OWrites[RetrieveUkProperty] = Json.writes[RetrieveUkProperty]

  implicit val reads: Reads[RetrieveUkProperty] = (
    (__ \ "ukOtherPropertyAnnualAdjustments").readNullable[RetrieveUkPropertyAdjustments] and
      (__ \ "ukOtherPropertyAnnualAllowances").readNullable[RetrieveUkPropertyAllowances]
  )(RetrieveUkProperty.apply _)

}
