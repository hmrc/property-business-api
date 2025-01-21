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

package v6.createAmendUkPropertyAnnualSubmission

import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import v6.createAmendUkPropertyAnnualSubmission.CreateAmendUkPropertyAnnualSubmissionSchema.{Def1, Def2}
import v6.createAmendUkPropertyAnnualSubmission.def1.Def1_CreateAmendUkPropertyAnnualSubmissionValidator
import v6.createAmendUkPropertyAnnualSubmission.def2.Def2_CreateAmendUkPropertyAnnualSubmissionValidator
import v6.createAmendUkPropertyAnnualSubmission.model.request.CreateAmendUkPropertyAnnualSubmissionRequestData

import javax.inject.Singleton

@Singleton
class CreateAmendUkPropertyAnnualSubmissionValidatorFactory {

  def validator(nino: String, businessId: String, taxYear: String, body: JsValue): Validator[CreateAmendUkPropertyAnnualSubmissionRequestData] = {

    val schema = CreateAmendUkPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear))
    schema match {
      case Valid(Def1)     => new Def1_CreateAmendUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear, body)
      case Valid(Def2)     => new Def2_CreateAmendUkPropertyAnnualSubmissionValidator(nino, businessId, taxYear, body)
      case Invalid(errors) => Validator.returningErrors(errors)
    }
  }

}
