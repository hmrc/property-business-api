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
  private def sampleUri(appConfig: AppConfig, nino: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/sample-endpoint"

  private def foreignPropertyUri(appConfig: AppConfig, nino: String, businessId: String, submissionId: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$businessId/period/$submissionId"

  private def foreignPropertyUriWithoutSubmissionId(appConfig: AppConfig, nino: String, businessId: String): String =
    s"/${appConfig.apiGatewayContext}/$nino/$businessId/period"

  //API resource links
  def sampleLink(appConfig: AppConfig, nino: String): Link =
    Link(href = sampleUri(appConfig, nino), method = GET, rel = SAMPLE_ENDPOINT_REL)

  def amendForeignProperty(appConfig: AppConfig, nino: String, businessId: String, submissionId: String): Link =
    Link(href = foreignPropertyUri(appConfig, nino, businessId, submissionId), method = PUT, rel = AMEND_FOREIGN_PROPERTY)

  def retrieveForeignProperty(appConfig: AppConfig, nino: String, businessId: String, submissionId: String): Link =
    Link(href = foreignPropertyUri(appConfig, nino, businessId, submissionId), method = GET, rel = RETRIEVE_FOREIGN_PROPERTY)

  def listForeignProperty(appConfig: AppConfig, nino: String, businessId: String): Link =
    Link(href = foreignPropertyUriWithoutSubmissionId(appConfig, nino, businessId), method = GET, rel = SELF)
}
