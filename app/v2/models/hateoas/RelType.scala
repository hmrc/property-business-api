/*
 * Copyright 2021 HM Revenue & Customs
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
  val SELF = "self"
  val AMEND_PROPERTY_PERIOD_SUMMARY = "amend-property-period-summary"
  val LIST_PROPERTY_PERIOD_SUMMARIES = "list-property-period-summaries"
  val CREATE_PROPERTY_PERIOD_SUMMARY = "create-property-period-summary"
  val AMEND_PROPERTY_ANNUAL_SUBMISSION = "amend-property-annual-submission"
  val DELETE_PROPERTY_ANNUAL_SUBMISSION = "delete-property-annual-submission"
  val AMEND_UK_PROPERTY_ANNUAL_SUBMISSION = "amend-uk-property-annual-submission"
}
