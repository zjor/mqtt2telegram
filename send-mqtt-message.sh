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

#TOPIC="79079907/binance"
#TOPIC="194676400/one"
TOPIC="765497286/test"


mqtt pub -h ${MQTT_HOST} -p 8883 -s -u ${MQTT_USER} --password ${MQTT_PASSWORD} -t ${TOPIC} -m "${PAYLOAD}"


