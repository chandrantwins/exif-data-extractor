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

1. Set up the following environment variables: 
	```sh
	WALDO_PHOTOS_JDBC_URL="jdbc:postgresql://server/database"
	WALDO_PHOTOS_JDBC_USER="username"
	WALDO_PHOTOS_JDBC_PASSWORD="password"
	```

2. Build the project
	```sh
	mvn clean install 
	```

3. Create the database schema:
	```sh
	mvn flyway:migrate 
	```

4. To run the project
	```sh
	mvn exec:java
	```

## Future Improvements:

1. Improve performance refactoring the naming convention of the file keys by adding the the timestamp prefix (e.g. `YYYYMMDD-hhmmss`) and retrieving only new files
2. Implement unit and integration tests.
3. Integrate with [Netflix's Archaius](https://github.com/Netflix/archaius) to allow dynamic configuration.
