# waldo-photos-exercise

Solution to the proposed engineering project to assess candidates. Problem Statement:

*Using any language and data-store of your choice, write an application that reads a set of photos from a network store (S3), 
parses the EXIF data from the photos and indexes the EXIF key/value pairs into a query-able store by unique photo.*

## Requirements

* Java 8
* PostgreSQL 9.3 

## Restrictions

1. Only JPEG files are supported. If a different file type is detected, a warning is logged.
2. Updated files will not be re-processed.
 
## How to run:

This solution requires a number of environment variables for runtime configuration. 

```sh
$ export WALDO_PHOTOS_JDBC_URL="jdbc:postgresql://server/database"
$ export WALDO_PHOTOS_JDBC_USER="username"
$ export WALDO_PHOTOS_JDBC_PASSWORD="password"
```

Build the project using [Maven 3.3](https://maven.apache.org/)
```sh
mvn clean install 
```

Create the database schema:
```sh
mvn flyway:migrate 
```

Finally run the project:
```sh
mvn exec:java
```

## Future Improvements:

1. Improve performance refactoring the naming convention of the file keys by adding the the timestamp prefix (e.g. `YYYYMMDD-hhmmss`) and retrieving only new files
2. Implement unit and integration tests.
3. Integrate with [Netflix's Archaius](https://github.com/Netflix/archaius) to allow dynamic configuration.
