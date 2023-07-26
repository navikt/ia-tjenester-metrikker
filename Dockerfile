FROM gcr.io/distroless/java17-debian11:latest
ENV TZ="Europe/Oslo"
COPY ./build/libs/ia-tjenester-metrikker-*.jar app.jar

EXPOSE 8080
CMD ["app.jar"]