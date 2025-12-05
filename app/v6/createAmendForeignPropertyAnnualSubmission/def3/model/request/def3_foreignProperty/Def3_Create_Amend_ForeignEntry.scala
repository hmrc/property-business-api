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

package v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.def3_foreignProperty

import play.api.libs.json.{Json, OFormat}
import shared.utils.EmptinessChecker
import shared.utils.EmptinessChecker.field

case class Def3_Create_Amend_ForeignEntry(propertyId: String,
                                          adjustments: Option[Def3_Create_Amend_ForeignAdjustments],
                                          allowances: Option[Def3_Create_Amend_ForeignAllowances])

object Def3_Create_Amend_ForeignEntry {

  implicit val emptinessChecker: EmptinessChecker[Def3_Create_Amend_ForeignEntry] = EmptinessChecker.use { body =>
    List(
      field("adjustments", body.adjustments),
      field("allowances", body.allowances)
    )
  }

  implicit val format: OFormat[Def3_Create_Amend_ForeignEntry] = Json.format[Def3_Create_Amend_ForeignEntry]
}
