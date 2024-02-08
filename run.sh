#!/usr/bin/env bash

docker-compose up -d

./gradlew compileKotlin --continuous --parallel --build-cache --configuration-cache --no-daemon &

sleep 5

./gradlew bootRun --args='--spring.profiles.active=compose' --no-daemon

docker-compose down -v

pkill -P $$
