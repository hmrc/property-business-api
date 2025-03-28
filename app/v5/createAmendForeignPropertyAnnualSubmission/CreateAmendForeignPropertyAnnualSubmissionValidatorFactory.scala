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

package v5.createAmendForeignPropertyAnnualSubmission

import cats.data.Validated.{Invalid, Valid}
import config.PropertyBusinessConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import v5.createAmendForeignPropertyAnnualSubmission.CreateAmendForeignPropertyAnnualSubmissionSchema.{Def1, Def2}
import v5.createAmendForeignPropertyAnnualSubmission.def1.Def1_CreateAmendForeignPropertyAnnualSubmissionValidator
import v5.createAmendForeignPropertyAnnualSubmission.def2.Def2_CreateAmendForeignPropertyAnnualSubmissionValidator
import v5.createAmendForeignPropertyAnnualSubmission.model.request.CreateAmendForeignPropertyAnnualSubmissionRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendForeignPropertyAnnualSubmissionValidatorFactory @Inject() (implicit config: PropertyBusinessConfig) {

  def validator(nino: String,
                businessId: String,
                taxYear: String,
                body: JsValue): Validator[CreateAmendForeignPropertyAnnualSubmissionRequestData] = {

    CreateAmendForeignPropertyAnnualSubmissionSchema.schemaFor(Some(taxYear)) match {
      case Valid(Def1)     => new Def1_CreateAmendForeignPropertyAnnualSubmissionValidator(nino, businessId, taxYear, body)
      case Valid(Def2)     => new Def2_CreateAmendForeignPropertyAnnualSubmissionValidator(nino, businessId, taxYear, body)
      case Invalid(errors) => Validator.returningErrors(errors)
    }
  }

}
