#!/bin/bash

source ../.env

kubectl create secret generic environment \
  --from-literal=MONGO_URI=${MONGO_URI} \
  --from-literal=MQTT_HOST=${MQTT_HOST} \
  --from-literal=MQTT_PORT=${MQTT_PORT} \
  --from-literal=MQTT_USER=${MQTT_USER} \
  --from-literal=MQTT_PASSWORD=${MQTT_PASSWORD} \
  --from-literal=TELEGRAM_TOKEN=${TELEGRAM_TOKEN} \
  --from-literal=TELEGRAM_BOT_USERNAME=${TELEGRAM_BOT_USERNAME} \
  -n app-mqtt2telegram