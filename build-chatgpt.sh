#!/usr/bin/env sh

set -e

./gradlew clean build -x test
