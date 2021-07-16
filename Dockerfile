FROM navikt/java:13
COPY ./build/libs/ia-tjenester-metrikker-*.jar ./

EXPOSE 8080
