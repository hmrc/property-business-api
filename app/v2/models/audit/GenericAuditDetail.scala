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

package v2.models.audit

import play.api.libs.functional.syntax._
import play.api.libs.json._
import v2.models.auth.UserDetails

case class GenericAuditDetail(versionNumber: String,
                              userType: String,
                              agentReferenceNumber: Option[String],
                              params: JsObject,
                              correlationId: String,
                              response: AuditResponse)

object GenericAuditDetail {

  implicit val writes: OWrites[GenericAuditDetail] = (
    (JsPath \ "versionNumber").write[String] and
      (JsPath \ "userType").write[String] and
      (JsPath \ "agentReferenceNumber").writeNullable[String] and
      JsPath.write[Map[String, JsValue]].contramap((p: JsObject) => p.value.toMap) and
      (JsPath \ "X-CorrelationId").write[String] and
      (JsPath \ "response").write[AuditResponse]
  )(unlift(GenericAuditDetail.unapply))

  def apply[A: OWrites](userDetails: UserDetails, params: A, correlationId: String, response: AuditResponse): GenericAuditDetail = {

    GenericAuditDetail(
      versionNumber = "2.0",
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      params = Json.toJsObject(params),
      correlationId = correlationId,
      response = response
    )
  }
}

case class FlattenedGenericAuditDetail(versionNumber: Option[String],
                                       userType: String,
                                       agentReferenceNumber: Option[String],
                                       params: Map[String, String],
                                       request: Option[JsValue],
                                       `X-CorrelationId`: String,
                                       response: String,
                                       httpStatusCode: Int,
                                       errorCodes: Option[Seq[String]],
                                       responseBody: Option[JsValue])

object FlattenedGenericAuditDetail {

  implicit val writes: OWrites[FlattenedGenericAuditDetail] = (
    (JsPath \ "versionNumber").writeNullable[String] and
      (JsPath \ "userType").write[String] and
      (JsPath \ "agentReferenceNumber").writeNullable[String] and
      JsPath.write[Map[String, String]] and
      JsPath.writeNullable[JsValue] and
      (JsPath \ "X-CorrelationId").write[String] and
      (JsPath \ "response").write[String] and
      (JsPath \ "httpStatusCode").write[Int] and
      (JsPath \ "errorCodes").writeNullable[Seq[String]] and
      JsPath.writeNullable[JsValue]
  )(unlift(FlattenedGenericAuditDetail.unapply))

  def apply(versionNumber: Option[String] = None,
            userDetails: UserDetails,
            params: Map[String, String],
            request: Option[JsValue],
            `X-CorrelationId`: String,
            auditResponse: AuditResponse): FlattenedGenericAuditDetail = {

    FlattenedGenericAuditDetail(
      versionNumber = versionNumber,
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      params = params,
      request = request,
      `X-CorrelationId` = `X-CorrelationId`,
      response = if (auditResponse.errors.exists(_.nonEmpty)) "error" else "success",
      httpStatusCode = auditResponse.httpStatus,
      errorCodes = auditResponse.errors.map(_.map(_.errorCode)),
      responseBody = auditResponse.body
    )
  }
}
