#!/usr/bin/env sh

set -e

cd chatgpt-java
./gradlew clean build -x test
