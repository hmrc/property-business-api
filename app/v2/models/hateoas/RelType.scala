/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.models.hateoas

object RelType {
  val SELF                                                = "self"
  val AMEND_FOREIGN_PROPERTY_PERIOD_SUMMARY               = "amend-foreign-property-period-summary"
  val CREATE_FOREIGN_PROPERTY_PERIOD_SUMMARY              = "create-foreign-property-period-summary"
  val RETRIEVE_FOREIGN_PROPERTY_PERIOD_SUMMARY            = "retrieve-foreign-property-period-summary"
  val CREATE_AND_AMEND_FOREIGN_PROPERTY_ANNUAL_SUBMISSION = "create-and-amend-foreign-property-annual-submission"
  val RETRIEVE_FOREIGN_PROPERTY_ANNUAL_SUBMISSION         = "retrieve-foreign-property-annual-submission"

  val CREATE_UK_PROPERTY_PERIOD_SUMMARY              = "create-uk-property-period-summary"
  val AMEND_UK_PROPERTY_PERIOD_SUMMARY               = "amend-uk-property-period-summary"
  val RETRIEVE_UK_PROPERTY_PERIOD_SUMMARY            = "retrieve-uk-property-period-summary"
  val CREATE_AND_AMEND_UK_PROPERTY_ANNUAL_SUBMISSION = "create-and-amend-uk-property-annual-submission"
  val RETRIEVE_UK_PROPERTY_ANNUAL_SUBMISSION         = "retrieve-uk-property-annual-submission"

  val RETRIEVE_HISTORIC_FHL_UK_PROPERTY_ANNUAL_SUBMISSION = "retrieve-uk-property-historic-fhl-annual-submission"

  val CREATE_AND_AMEND_HISTORIC_NON_FHL_UK_PROPERTY_ANNUAL_SUBMISSION = "create-and-amend-uk-property-historic-non-fhl-annual-submission"
  val RETRIEVE_HISTORIC_NON_FHL_UK_PROPERTY_ANNUAL_SUBMISSION         = "retrieve-uk-property-historic-non-fhl-annual-submission"
  val DELETE_HISTORIC_NON_FHL_UK_PROPERTY_ANNUAL_SUBMISSION           = "delete-uk-property-historic-non-fhl-annual-submission"

  val AMEND_HISTORIC_UK_FHL_PROPERTY_INCOME_EXPENSES_PERIOD_SUMMARY    = "amend-historic-uk-fhl-property-income-expenses-period-summary"
  val RETRIEVE_HISTORIC_UK_FHL_PROPERTY_INCOME_EXPENSES_PERIOD_SUMMARY = "retrieve-historic-uk-fhl-property-income-expenses-period-summary"

  val LIST_PROPERTY_PERIOD_SUMMARIES    = "list-property-period-summaries"
  val DELETE_PROPERTY_ANNUAL_SUBMISSION = "delete-property-annual-submission"
}
