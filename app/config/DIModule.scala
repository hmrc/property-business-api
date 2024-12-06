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

package config

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import play.api.{Configuration, Environment}
import play.api.libs.json.{OWrites, Reads}
import v5.createAmendUkPropertyAnnualSubmission.def1.model.request.ukProperty.CreateAmendUkPropertyAllowances
import v5.createAmendUkPropertyAnnualSubmission.def2.model.request.Allowances
import v5.retrieveUkPropertyAnnualSubmission._

class DIModule(env: Environment, conf: Configuration) extends AbstractModule {

  private val propName = {
    val isPropRenamed = conf.get[Boolean]("feature-switch.renameCostOfReplacingDomesticItems.enabled")
    if (isPropRenamed) "costOfReplacingDomesticItems" else "costOfReplacingDomesticGoods"
  }

  @Provides
  @Named("w1")
  def def1RetrieveAllowancesProvider: OWrites[def1.model.response.ukProperty.RetrieveUkPropertyAllowances] =
    def1.model.response.ukProperty.RetrieveUkPropertyAllowances.writes(propName)

  @Provides
  @Named("w2")
  def def2RetrieveAllowances: OWrites[def2.model.response.RetrieveUkPropertyAllowances] =
    def2.model.response.RetrieveUkPropertyAllowances.writes(propName)

  @Provides
  @Named("r1")
  def def1CreateAllowances: Reads[CreateAmendUkPropertyAllowances] = CreateAmendUkPropertyAllowances.reads(propName)

  @Provides
  @Named("r2")
  def def2CreateAllowances: Reads[Allowances] = Allowances.reads(propName)

  override def configure(): Unit = {
    bind(classOf[AppConfig]).to(classOf[AppConfigImpl]).asEagerSingleton()
  }

}
