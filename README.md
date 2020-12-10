Property Business API
========================
The Property Business API allows software packages to create, retrieve, amend and delete data for a property business.

## Requirements

- Scala 2.12.x
- Java 8
- sbt > 1.3.7
- [Service Manager](https://github.com/hmrc/service-manager)

## Running the microservice
Run from the console using: `sbt run` (starts on port 7798 by default)

Start the service manager profile: `sm --start MTDFB_PROPERTY_BUSINESS`
 
## Run Tests
Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## To view the RAML
To view documentation locally ensure the Property Business API is running, and run api-documentation-frontend:

```
./run_local_with_dependencies.sh
```

Then go to http://localhost:9680/api-documentation/docs/api/preview and use this port and version:

```
http://localhost:7798/api/conf/1.0/application.raml
```

## Reporting Issues
You can create a GitHub issue [here](https://github.com/hmrc/property-business-api/issues)

## API Reference / Documentation 
Available on the [HMRC Developer Hub](https://https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/property-business-api/1.0)


## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
