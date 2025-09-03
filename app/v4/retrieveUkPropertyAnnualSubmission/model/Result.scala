/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.retrieveUkPropertyAnnualSubmission.model

import v4.retrieveUkPropertyAnnualSubmission.model.response.Def1_RetrieveUkPropertyAnnualSubmissionResponse

sealed trait Result

case class UkResult(response: Def1_RetrieveUkPropertyAnnualSubmissionResponse) extends Result

case object NonUkResult extends Result
