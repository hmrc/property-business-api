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

package common.controllers.validators.resolvers

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import shared.controllers.validators.resolvers.ResolverSupport
import shared.models.errors.MtdError

import scala.util.matching.Regex

object ResolveUuid extends ResolverSupport {

  private val uuidRegex: Regex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$".r

  private def resolver[T](error: MtdError, toType: String => T): Resolver[String, T] = value =>
    if (uuidRegex.matches(value)) Valid(toType(value)) else Invalid(List(error))

  def apply[T](value: String, error: MtdError)(toType: String => T): Validated[Seq[MtdError], T] =
    resolver(error, toType)(value)

  def apply[T](value: Option[String], error: MtdError)(toType: String => T): Validated[Seq[MtdError], Option[T]] =
    resolver(error, toType).resolveOptionally(value)

}
