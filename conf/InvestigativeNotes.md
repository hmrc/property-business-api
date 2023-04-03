# DELETE and DELETE_STATEFUL

Investigation triggered by [this github issue](https://github.com/hmrc/property-business-api/pull/372/files#r1153009256).

#### The Ask: Currently vendors must add a "DELETE" gov-test-scenario header to test a DELETE endpoint. How can we simplify this and let vendors just use a DEFAULT gov-test-scenario instead?

#### Proposed solution:

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