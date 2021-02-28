FROM navikt/java:13
COPY ./build/libs/ia-tjenester-metrikker-*.jar app.jar

EXPOSE 8080