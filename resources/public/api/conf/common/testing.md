
Some APIs may be marked [test only](https://developer.service.hmrc.gov.uk/api-documentation/docs/testing). 
This means that they are not available for use in production and may change.

You can use the Sandbox environment to test this API. You can test different scenarios in the Sandbox by passing the
Gov-Test-Scenario header. 
The Sandbox also allows you to perform [stateful and dynamic testing](https://developer.service.hmrc.gov.uk/guides/income-tax-mtd-end-to-end-service-guide/documentation/how-to-integrate.html#sandbox-testing) for some APIs. The ‘Test
data’ section under each endpoint explains the test scenarios that can be simulated.

If you have a specific testing need that is not supported in the Sandbox, [contact our Software Developers Support Team](https://developer.service.hmrc.gov.uk/developer/support).
Some endpoints may be marked '\[test only\]', which means that they are not available for use in Production and may
change.

### Stateful

Some endpoints support STATEFUL gov test scenarios. Stateful scenarios work with groups of endpoints that represent
particular types of submissions. For each type you can POST (or PUT) to submit or amend data, GET to retrieve or list
data and DELETE to delete data. For example, with a STATEFUL gov test scenario a retrieval will return data based on
what you submitted.

The following groups are stateful in the sandbox:

- UK Property Business Annual Submission
- Foreign Property Annual Submission
- UK Property Income and Expenses Period Summary
- Foreign Property Income and Expenses Period Summary
- Historic FHL UK Property Business Annual Submission
- Historic FHL UK Property Income and Expenses Period Summary
- Historic non-FHL UK Property Business Annual Submission
- Historic non-FHL UK Property Income and Expenses Period Summary


