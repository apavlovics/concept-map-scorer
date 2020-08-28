# Concept Map Scorer

This application allows scoring [concept maps](https://en.wikipedia.org/wiki/Concept_map) using different
methods. Concept maps (also known as *semantic networks*) are used in education to assess students’ knowledge.

The application has been developed as part of a master’s thesis. Check [author’s personal
website](https://continuum.lv/#thesis) for more information and the entire thesis in Latvian.

## System Requirements

The application is tested to work with:

* Java 11
* Maven 3.6

## Testing

To run unit tests, go to the project’s root folder and execute:

    mvn test

To run both unit and integration tests, go to the project’s root and execute:

    mvn verify

## Packaging & Running

To package the application, go to its root folder and execute:

    mvn package

Once packaged, the application can be run via the generated executable JAR file. It is typically named
*concept-map-scorer-{version}.jar* and located in the *target* folder.
