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

package v5.propertyPeriodSummary.list

import shared.controllers.validators.Validator
import v5.propertyPeriodSummary.list.def1.Def1_ListPropertyPeriodSummariesValidator
import v5.propertyPeriodSummary.list.model.request.ListPropertyPeriodSummariesRequestData

import javax.inject.Singleton

@Singleton
class ListPropertyPeriodSummariesValidatorFactory {

  def validator(nino: String, businessId: String, taxYear: String): Validator[ListPropertyPeriodSummariesRequestData] = {

    new Def1_ListPropertyPeriodSummariesValidator(
      nino,
      businessId,
      taxYear
    )
  }

}
