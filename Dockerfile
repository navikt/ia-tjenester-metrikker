FROM navikt/java:13
COPY ./build/libs/ia-tjenester-metrikker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
