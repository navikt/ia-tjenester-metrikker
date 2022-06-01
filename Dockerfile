FROM navikt/java:17
COPY ./build/libs/ia-tjenester-metrikker-*.jar app.jar

EXPOSE 8080
