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

package v6.historicFhlUkPropertyPeriodSummary.list

import shared.controllers.validators.Validator
import v6.historicFhlUkPropertyPeriodSummary.list.def1.Def1_ListHistoricFhlUkPropertyPeriodSummariesValidator
import v6.historicFhlUkPropertyPeriodSummary.list.model.request.ListHistoricFhlUkPropertyPeriodSummariesRequestData

import javax.inject.Singleton

@Singleton
class ListHistoricFhlUkPropertyPeriodSummariesValidatorFactory {

  def validator(nino: String): Validator[ListHistoricFhlUkPropertyPeriodSummariesRequestData] =
    new Def1_ListHistoricFhlUkPropertyPeriodSummariesValidator(nino)

}
