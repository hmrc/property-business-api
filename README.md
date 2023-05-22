Property Business API
========================
The Property Business API allows software packages to create, retrieve, amend and delete data for a property business.

- UK Property (FHL & Non-FHL)
- Foreign Property (FHL & Non-FHL)
- Historic UK Property (FHL & Non-FHL)

## Requirements

- Scala 2.13.x
- Java 8
- sbt 1.7.x
- [Service Manager](https://github.com/hmrc/service-manager)

## Running the microservice

Run from the console using: `sbt run` (starts on port 7798 by default)

Start the service manager profile: `sm --start MTDFB_PROPERTY_BUSINESS`

## Run Tests

Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## Viewing Open API Spec (OAS) docs

To view documentation locally ensure the Property Business API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`

Then go to http://localhost:9680/api-documentation/docs/openapi/preview and use this port and version:
`http://localhost:7798/api/conf/2.0/application.yaml`

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog/wiki)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [HMRC Developer Hub](https://https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/property-business-api)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

