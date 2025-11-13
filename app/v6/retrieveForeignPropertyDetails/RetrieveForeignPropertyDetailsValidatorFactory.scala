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

package v6.retrieveForeignPropertyDetails

import cats.data.Validated.{Invalid, Valid}
import shared.controllers.validators.Validator
import v6.retrieveForeignPropertyDetails.def1.Def1_RetrieveForeignPropertyDetailsValidator
import v6.retrieveForeignPropertyDetails.model.request.RetrieveForeignPropertyDetailsRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveForeignPropertyDetailsValidatorFactory @Inject() {

  def validator(nino: String, businessId: String, taxYear: String, propertyId: Option[String]): Validator[RetrieveForeignPropertyDetailsRequestData] =
    RetrieveForeignPropertyDetailsSchema.schemaFor(taxYear) match {
      case Valid(RetrieveForeignPropertyDetailsSchema.Def1) =>
        new Def1_RetrieveForeignPropertyDetailsValidator(nino, businessId, taxYear, propertyId)
      case Invalid(errors) => Validator.returningErrors(errors)
    }

}
