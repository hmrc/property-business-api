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

package api.controllers.validators.resolvers

import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import scala.math.Ordered.orderingToOrdered

trait Resolvers {
  type SimpleResolver[In, Out] = In => Validated[Seq[MtdError], Out]

  implicit class SimpleResolverOps[In, Out1](resolver: In => Validated[Seq[MtdError], Out1]) {
    // To apply/compose resolvers in order
    def andThenF[Out2](other: SimpleResolver[Out1, Out2]): SimpleResolver[In, Out2] = i => resolver(i).andThen(other)

    // To apply further re-usable validation
    def thenValidate(other: SimpleResolver[Out1, Out1]): SimpleResolver[In, Out1] = resolver.andThenF(other)
  }

  def satisfies[A](errors: Seq[MtdError])(predicate: A => Boolean): SimpleResolver[A, A] =
    a => if (predicate(a)) Valid(a) else Invalid(errors)

  def satisfies[A](error: MtdError)(predicate: A => Boolean): SimpleResolver[A, A] =
    satisfies(List(error))(predicate)

  def inRange[A: Ordering](minAllowed: A, maxAllowed: A, error: MtdError): SimpleResolver[A, A] =
    satisfiesMin[A](minAllowed, error) thenValidate satisfiesMax[A](maxAllowed, error)

  def satisfiesMin[A: Ordering](minAllowed: A, error: MtdError): SimpleResolver[A, A] = satisfies(error)(minAllowed <= _)
  def satisfiesMax[A: Ordering](maxAllowed: A, error: MtdError): SimpleResolver[A, A] = satisfies(error)(_ <= maxAllowed)

}

object Resolvers extends Resolvers
