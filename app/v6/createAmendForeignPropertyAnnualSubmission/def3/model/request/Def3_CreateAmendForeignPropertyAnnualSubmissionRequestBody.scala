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

package v6.createAmendForeignPropertyAnnualSubmission.def3.model.request

import play.api.libs.json.*
import v6.createAmendForeignPropertyAnnualSubmission.def3.model.request.def3_foreignProperty.Def3_Create_Amend_ForeignEntry
import v6.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestBody

case class Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody(foreignProperty: Seq[def3_foreignProperty.Def3_Create_Amend_ForeignEntry])
    extends CreateAmendForeignPropertyAnnualSubmissionRequestBody

object Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody {

  implicit val format: OFormat[Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody] =
    Json.format[Def3_CreateAmendForeignPropertyAnnualSubmissionRequestBody]

}
