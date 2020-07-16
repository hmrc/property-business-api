package v1.controllers.requestParsers.foreignPropertyAnnualSubmission

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import v1.controllers.requestParsers.RequestParser

class AmendForeignPropertyAnnualSubmission @Inject()(val validator: AmendForeignPropertyAnnualSubmissionValidator)
  extends RequestParser[RawData???, Request???] {

  override protected def requestFor(data: RawData???): Request??? =
    Request???(Nino(data.nino), data.businessId, data.taxYear, data.body.as[RequestBody???])

}
