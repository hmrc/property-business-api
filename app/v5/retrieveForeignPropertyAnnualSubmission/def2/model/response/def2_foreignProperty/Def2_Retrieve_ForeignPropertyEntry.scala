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

package v5.retrieveForeignPropertyAnnualSubmission.def2.model.response.def2_foreignProperty

import play.api.libs.json.{Json, OFormat}

case class Def2_Retrieve_ForeignPropertyEntry(countryCode: String,
                                              adjustments: Option[Def2_Retrieve_ForeignPropertyAdjustments],
                                              allowances: Option[Def2_Retrieve_ForeignPropertyAllowances])

object Def2_Retrieve_ForeignPropertyEntry {
  implicit val format: OFormat[Def2_Retrieve_ForeignPropertyEntry] = Json.format[Def2_Retrieve_ForeignPropertyEntry]
}