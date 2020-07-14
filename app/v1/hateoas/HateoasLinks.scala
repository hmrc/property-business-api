/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.hateoas

import config.AppConfig
import v1.models.hateoas.Link
import v1.models.hateoas.Method._
import v1.models.hateoas.RelType._

trait HateoasLinks {

  //Domain URIs

  private def foreignPropertyUri(appConfig: AppConfig, nino: String, businessId: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$businessId/period"

  private def foreignPropertySubmissionUri(appConfig: AppConfig, nino: String, businessId: String, submissionId: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$businessId/period/$submissionId"

  //API resource links
  def listForeignProperties(appConfig: AppConfig, nino: String, businessId: String, rel: String = SELF): Link =
    Link(href = foreignPropertyUri(appConfig, nino, businessId), method = GET, rel = rel)

  def createForeignProperty(appConfig: AppConfig, nino: String, businessId: String): Link =
    Link(href = foreignPropertyUri(appConfig, nino, businessId), method = POST, rel = CREATE_PROPERTY_PERIOD_SUMMARY)

  def amendForeignProperty(appConfig: AppConfig, nino: String, businessId: String, submissionId: String): Link =
    Link(href = foreignPropertySubmissionUri(appConfig, nino, businessId, submissionId), method = PUT, rel = AMEND_PROPERTY_PERIOD_SUMMARY)

  def retrieveForeignProperty(appConfig: AppConfig, nino: String, businessId: String, submissionId: String): Link =
    Link(href = foreignPropertySubmissionUri(appConfig, nino, businessId, submissionId), method = GET, rel = SELF)
}
