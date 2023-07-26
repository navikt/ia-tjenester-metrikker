FROM gcr.io/distroless/java11-debian11
ENV TZ="Europe/Oslo"
COPY ./build/libs/ia-tjenester-metrikker-*.jar app.jar

EXPOSE 8080
