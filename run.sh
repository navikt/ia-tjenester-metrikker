#!/usr/bin/env bash

docker-compose up -d

./gradlew compileKotlin --continuous --parallel --build-cache --configuration-cache &

sleep 5

./gradlew bootRun --args='--spring.profiles.active=compose'

docker-compose down -v

pkill -P $$
