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

package v4.retrieveUkPropertyAnnualSubmission.def1.model.response.def1_ukNonFhlProperty

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class Def1_Retrieve_UkNonFhlProperty(adjustments: Option[Def1_Retrieve_UkNonFhlPropertyAdjustments],
                                          allowances: Option[Def1_Retrieve_UkNonFhlPropertyAllowances])

object Def1_Retrieve_UkNonFhlProperty {
  implicit val writes: OWrites[Def1_Retrieve_UkNonFhlProperty] = Json.writes[Def1_Retrieve_UkNonFhlProperty]

  implicit val reads: Reads[Def1_Retrieve_UkNonFhlProperty] = (
    (__ \ "ukOtherPropertyAnnualAdjustments").readNullable[Def1_Retrieve_UkNonFhlPropertyAdjustments] and
      (__ \ "ukOtherPropertyAnnualAllowances").readNullable[Def1_Retrieve_UkNonFhlPropertyAllowances]
  )(Def1_Retrieve_UkNonFhlProperty.apply _)

}
