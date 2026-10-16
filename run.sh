#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mvn -q package
java -jar target/notification-gateway-1.0.0.jar
