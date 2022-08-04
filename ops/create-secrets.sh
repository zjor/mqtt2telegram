#!/bin/bash

source ../.env

kubectl delete secret environment -n app-mqtt2telegram

kubectl create secret generic environment \
  --from-literal=MONGO_URI=${MONGO_URI} \
  --from-literal=MQTT_HOST=${MQTT_HOST} \
  --from-literal=MQTT_PORT=${MQTT_PORT} \
  --from-literal=MQTT_USER=${MQTT_USER} \
  --from-literal=MQTT_PASSWORD=${MQTT_PASSWORD} \
  --from-literal=TELEGRAM_USER_ID=${TELEGRAM_USER_ID} \
  --from-literal=TELEGRAM_TOKEN=${TELEGRAM_TOKEN} \
  --from-literal=TELEGRAM_BOT_USERNAME=${TELEGRAM_BOT_USERNAME} \
  --from-literal=API_BASE_URL=${API_BASE_URL} \
  -n app-mqtt2telegram