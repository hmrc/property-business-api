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

package v5.createAmendForeignPropertyAnnualSubmission.def2.model.request

import play.api.libs.json._
import v5.createAmendForeignPropertyAnnualSubmission.def2.model.request.def2_foreignProperty.Def2_Create_Amend_ForeignEntry
import v5.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestBody


case class Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody(foreignProperty: Option[Seq[Def2_Create_Amend_ForeignEntry]])
    extends CreateAmendForeignPropertyAnnualSubmissionRequestBody

object Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody {

  implicit val format: OFormat[Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody] = Json.format[Def2_CreateAmendForeignPropertyAnnualSubmissionRequestBody]

}