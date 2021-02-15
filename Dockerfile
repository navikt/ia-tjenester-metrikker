FROM navikt/java:13
COPY ./build/libs/ia-tjenester-metrikker-all.jar app.jar

EXPOSE 8222