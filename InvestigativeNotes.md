# DELETE and DELETE_STATEFUL

Investigation triggered by [this github comment](https://github.com/hmrc/property-business-api/pull/372/files#r1153009256).

#### The Ask: Currently vendors must add a "DELETE" or "STATEFUL_DELETE" gov-test-scenario header to test some DELETE endpoints. How can we simplify this and let vendors just use a DEFAULT gov-test-scenario instead?

###  Expansion of Jeremy's solution

Summary: Instead of matching according to the request method, the API will add in a "vendor-intent" header before forwarding the request to the mtd-sa-api-stub.

More detail:

1. Vendor submits a DELETE request to **API#1430 - Delete a Historic FHL UK Property Business Annual Submission**, with the GTS header set as "STATEFUL".
2. Property-business-api routes this request to the `DeleteHistoricUkPropertyAnnualSubmissionController` using the request method and url in the `conf/v2r7c.routes` file.
3. The request continues through the controller until it gets to the service, `DeleteHistoricUkPropertyAnnualSubmissionService`
4. Since we already know that the vendor's request is to DELETE and that it is Stateful, we add in a "vendor-intent" header, like so:
```scala
val updatedOtherHeaders = ("vendor-intent", "DELETE") +: hc.otherHeaders
val requiredHeaders = hc.copy(otherHeaders = updatedOtherHeaders)
```
and pass this through to mtd-sa-api-stub via the connector.

5. mtd-sa-api-stub searches through its database. If two stubs match the normal key criteria, it checks the "intent" header to distinguish between them. The stub then responds with the correct template as normal.

This will mean adding an optional "vendor-intent" column to the database.

One way of doing this is to add "vendor-intent" to the "request" section of the relevant json config. 

So for example, `DeleteStateful.json` may look like this: 

```json
{
  "request": {
    "url": "/income-tax/nino/(?<taxableEntityId>.{9})/uk-properties/other/annual-summaries/(?<taxYear>.{4})",
    "method": "PUT",
    "govTestScenario": "STATEFUL",
    "version": "2.0",
    "vendorIntent": "DELETE"
  },
  "state": {
    "action": "delete",
    "key": {
      "resource": {
        "literal": "property_historic_non-fhl_annual"
      },
      "taxableEntityId": {
        "pathParameter": "taxableEntityId"
      },
      "taxYear": {
        "pathParameter": "taxYear"
      }
    }
  },
  "response": {
    "type": "template",
    "template": "endpoints.utils.jst.DeleteStatefulResponse",
    "extraParameters": {
      "notFoundErrorCode": "NOT_FOUND"
    }
  }
}
```

We would also need to update `ConfigRespository`, specifically the `findEndpointConfigsWith` method. Perhaps a tidier version of the below: 

```scala
 override def findEndpointConfigsWith(method: String, govTestScenario: String, version: String, vendorIntent: Option[String]=None): Future[Seq[EndpointConfig]] = {
  if(vendorIntent.nonEmpty){
    collection
      .find(
        and(
          equal("request.method", method),
          equal("request.govTestScenario", govTestScenario),
          equal("request.version", version),
          equal("request.vendorIntent", vendorIntent.get)
        )
      )
      .toFuture(
      )
  } else{
    collection
      .find(
        and(
          equal("request.method", method),
          equal("request.govTestScenario", govTestScenario),
          equal("request.version", version)
        )
      )
      .toFuture(
      )
  }

}
```

We may need to make similar changes to `findUrlsWith` too. 

Note: "vendor-Intent" isn't limited to request methods like "DELETE". We can make it any string we like, e.g. "FOREIGN_PROPERTY" if we think that is more descriptive. 

### Alternative solution considered - Modifying GTS Header 

**Rejected** because it ties the APIs to closely to the sandbox. APIs would have to know what the GTS is and what it means, which introduces unnecessary fragility. 

Summary: Instead of identifying if a vendor wishes to RETRIEVE or DELETE by a gov-test-scenario heading, we identify this by the request method. This eliminates the need for a special gov-test-scenario heading and also more closely imitates how the live environment behaves.

More detail:

1. Vendor submits a DELETE request to **API#1430 - Delete a Historic FHL UK Property Business Annual Submission**. Importantly, they leave the gov-test-scenario heading blank.
2. Property-business-api routes this request to the `DeleteHistoricUkPropertyAnnualSubmissionController` using the request method and url in the `conf/v2r7c.routes` file.
3. The request continues through the controller until it gets to the service, `DeleteHistoricUkPropertyAnnualSubmissionService`
4. Since we already know that the vendor's request is to DELETE, we add in a gov-test-scenario "DELETE" header, like so:
```scala
	val newOtherHeaders = ("Gov-Test-Scenario","DELETE") +: hc.otherHeaders  
	val requiredHeaders = hc.copy(otherHeaders = newOtherHeaders)
```
and pass this down to the connector. The mtd-api-stub then responds to the request as normal.

5. Since the service won't need to add a gov-test-scenario header in the live environment, we can feature-switch this functionality:
```scala
def isGovTestScenarioRequired(implicit featureSwitches: FeatureSwitches): Boolean = featureSwitches.isSandboxEnabled  
  
val requiredHeaders = if(isGovTestScenarioRequired){  
  //Sandbox: Add in gov-test-scenario here  
  val newOtherHeaders = ("Gov-Test-Scenario","DELETE") +: hc.otherHeaders  
  hc.copy(otherHeaders = newOtherHeaders)  
}else{  
   //Live: No GTS header required
  hc  
}
```

### Other strange situations and what to do about them

**STATEFUL vs. STATEFUL_DELETE**

This situation requires slightly more complex logic:

1.) Check if there is a "STATEFUL" GTS header and

2a) If there is, edit it to be "STATEFUL_DELETE".

2b) If there isn't, just add the normal "DELETE" GTS header

The service might look something like this:
```scala
val existingGTSHeader = hc.otherHeaders.find(e => e.toString.contains("Gov-Test-Scenario")).getOrElse(("ignored", "unused"))  
val indexToReplace    = hc.otherHeaders.indexOf(existingGTSHeader)  
  
val updatedOtherHeaders = if(existingGTSHeader.toString() == "(Gov-Test-Scenario,STATEFUL)"){  
  //Change "STATEFUL" to "STATEFUL_DELETE"  
  hc.otherHeaders.updated(indexToReplace, ("Gov-Test-Scenario", "STATEFUL_DELETE"))  
}else{  
  //Add in "DELETE" GTS header  
  ("Gov-Test-Scenario", "DELETE") +: hc.otherHeaders  
}  
  
val requiredHeaders = hc. copy(otherHeaders = updatedOtherHeaders)
```
This logic could be abstracted into its own function and used across all delete endpoints that require such specialised logic.

**UK_PROPERTY vs. FOREIGN_PROPERTY**

For example, **API#1598 & 1805 - Retrieve a UK Property Business Annual Submission**.

In my opinion, this endpoint isn't really used by two different property types. The "FOREIGN_PROPERTY" scenario is really just a way of getting "RULE_TYPE_OF_BUSINESS_INCORRECT".

We could rename the gov-test-scenario from "FOREIGN_PROPERTY" to "TYPE_OF_BUSINESS_INCORRECT" if vendors are puzzled by this name.

