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

package mocks

import play.api.libs.json.{Json, OWrites, Reads}
import v5.createAmendUkPropertyAnnualSubmission.def1.model.request.ukProperty.CreateAmendUkPropertyAllowances
import v5.createAmendUkPropertyAnnualSubmission.def2.model.request.Allowances
import v5.retrieveUkPropertyAnnualSubmission.def2.model.response.RetrieveUkPropertyAllowances
import v5.retrieveUkPropertyAnnualSubmission.model.response.RetrieveUkPropertyAnnualSubmissionResponse.{
  Def1_RetrieveUkPropertyAnnualSubmissionResponse,
  Def2_RetrieveUkPropertyAnnualSubmissionResponse
}
import v5.retrieveUkPropertyAnnualSubmission.{def1, def2}
import v5.retrieveUkPropertyAnnualSubmission.def2.model.response.{RetrieveUkProperty => Def2_RetrieveUkProperty}
import v5.retrieveUkPropertyAnnualSubmission.def1.model.response.ukProperty.{RetrieveUkProperty => Def1_RetrieveUkProperty}

trait MockJsonReadsWrites {
  val propertyName = "costOfReplacingDomesticItems"

  implicit val writes1: OWrites[def1.model.response.ukProperty.RetrieveUkPropertyAllowances] =
    def1.model.response.ukProperty.RetrieveUkPropertyAllowances.writes(propertyName)

  implicit val writes2: OWrites[def2.model.response.RetrieveUkPropertyAllowances] =
    RetrieveUkPropertyAllowances.writes(propertyName)

  implicit val reads1: Reads[CreateAmendUkPropertyAllowances] =
    CreateAmendUkPropertyAllowances.reads(propertyName)

  implicit val reads2: Reads[Allowances] =
    Allowances.reads(propertyName)

  implicit def def1ResponseWrites(implicit w: OWrites[Def1_RetrieveUkProperty]): OWrites[Def1_RetrieveUkPropertyAnnualSubmissionResponse] =
    Json.writes[Def1_RetrieveUkPropertyAnnualSubmissionResponse]

  implicit def def2ResponseWrites(implicit w: OWrites[Def2_RetrieveUkProperty]): OWrites[Def2_RetrieveUkPropertyAnnualSubmissionResponse] =
    Json.writes[Def2_RetrieveUkPropertyAnnualSubmissionResponse]

}
