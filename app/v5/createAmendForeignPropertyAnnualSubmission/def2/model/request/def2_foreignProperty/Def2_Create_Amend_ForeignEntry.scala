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

package v5.createAmendForeignPropertyAnnualSubmission.def2.model.request.def2_foreignProperty

import play.api.libs.json.{Json, OFormat}
import shapeless.HNil
import utils.EmptinessChecker

case class Def2_Create_Amend_ForeignEntry(countryCode: String,
                                                adjustments: Option[Def2_Create_Amend_ForeignAdjustments],
                                                allowances: Option[Def2_Create_Amend_ForeignAllowances])

object Def2_Create_Amend_ForeignEntry {

  implicit val emptinessChecker: EmptinessChecker[Def2_Create_Amend_ForeignEntry] = EmptinessChecker.use { body =>
    "adjustments"  -> body.adjustments ::
      "allowances" -> body.allowances :: HNil
  }

  implicit val format: OFormat[Def2_Create_Amend_ForeignEntry] = Json.format[Def2_Create_Amend_ForeignEntry]
}
