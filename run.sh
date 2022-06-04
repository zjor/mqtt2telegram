#!/bin/bash

JAR=mqtt2telegram-jar-with-dependencies.jar

source .env

java -Djdk.tls.client.protocols=TLSv1.2 -jar ./target/${JAR}
