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

package auth

import api.models.errors.NinoFormatError
import api.services.DownstreamStub
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}

class PropertyBusinessApiAuthSupportingAgentsAllowedISpec extends AuthSupportingAgentsAllowedISpec {

  val callingApiVersion = "5.0"

  val supportingAgentsAllowedEndpoint = "retrieve-uk-property-annual-submission"

  val mtdUrl = s"/uk/AA123/XAIS12345678910/annual/2022-23"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  val downstreamUri = s"/income-tax/business/property/annual"

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  override val downstreamSuccessStatus: Int  = BAD_REQUEST

  override val expectedMtdSuccessStatus: Int = BAD_REQUEST

  val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.toJson(NinoFormatError)
  )

}
