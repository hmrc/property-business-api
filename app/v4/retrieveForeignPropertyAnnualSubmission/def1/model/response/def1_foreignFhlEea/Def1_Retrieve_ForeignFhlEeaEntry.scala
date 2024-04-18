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

package v4.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignFhlEea

import play.api.libs.json.{Json, OFormat}
import v4.retrieveForeignPropertyAnnualSubmission.def1.model.response.def1_foreignFhlEea.Def1_Retrieve_ForeignFhlEeaAdjustments

case class Def1_Retrieve_ForeignFhlEeaEntry(adjustments: Option[Def1_Retrieve_ForeignFhlEeaAdjustments],
                                            allowances: Option[Def1_Retrieve_ForeignFhlEeaAllowances])

object Def1_Retrieve_ForeignFhlEeaEntry {
  implicit val format: OFormat[Def1_Retrieve_ForeignFhlEeaEntry] = Json.format[Def1_Retrieve_ForeignFhlEeaEntry]
}
