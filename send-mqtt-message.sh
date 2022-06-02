#!/bin/bash

source .env

PAYLOAD="Header
======
[google](https://www.google.com)
\`notice\`
\`\`\`json
{
  \"attr\": \"value\"
}
\`\`\`
**TODO**
- one
- two
- three"

TOPIC="79079907/binance"

mqtt pub -h ${MQTT_HOST} -p 8883 -s -u ${MQTT_USER} --password ${MQTT_PASSWORD} -t ${TOPIC} -m "${PAYLOAD}"

