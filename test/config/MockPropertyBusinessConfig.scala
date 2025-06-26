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

package config

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite

trait MockPropertyBusinessConfig extends TestSuite with MockFactory {

  implicit val mockPropertyBusinessConfig: PropertyBusinessConfig = mock[PropertyBusinessConfig]

  def ukMinimumTaxYearMock(year: String): CallHandler[String] =
    (() => mockPropertyBusinessConfig.ukMinimumTaxYear).expects().returning(year).anyNumberOfTimes()

  def foreignMinimumTaxYearMock(year: String): CallHandler[String] =
    (() => mockPropertyBusinessConfig.foreignMinimumTaxYear).expects().returning(year).anyNumberOfTimes()

  def historicMinimumTaxYearMock(year: String): CallHandler[String] =
    (() => mockPropertyBusinessConfig.historicMinimumTaxYear).expects().returning(year).anyNumberOfTimes()

  def historicMaximumTaxYearMock(year: String): CallHandler[String] =
    (() => mockPropertyBusinessConfig.historicMaximumTaxYear).expects().returning(year).anyNumberOfTimes()

  trait SetupConfig {
    def ukMinimumTaxYear: String                   = "2022-23"
    def setUkMinimumTaxYear(): CallHandler[String] = ukMinimumTaxYearMock(ukMinimumTaxYear)
    setUkMinimumTaxYear()

    def foreignMinimumTaxYear: String                   = "2021-22"
    def setForeignMinimumTaxYear(): CallHandler[String] = foreignMinimumTaxYearMock(foreignMinimumTaxYear)
    setForeignMinimumTaxYear()

    def historicMinimumTaxYear: String                   = "2017-18"
    def setHistoricMinimumTaxYear(): CallHandler[String] = historicMinimumTaxYearMock(historicMinimumTaxYear)
    setHistoricMinimumTaxYear()

    def historicMaximumTaxYear: String                   = "2021-22"
    def setHistoricMaximumTaxYear(): CallHandler[String] = historicMaximumTaxYearMock(historicMaximumTaxYear)
    setHistoricMaximumTaxYear()

  }

}
