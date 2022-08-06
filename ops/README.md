# Helm chart maintenance

1. Create namespace `kubectl create namespace app-mqtt2telegram`
2. Create secrets:
   1. `source .env`
   2. 
```bash
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
```

3. Deploy with Helm
```
helm upgrade --namespace app-mqtt2telegram --install mqtt2telegram --set version=latest ./ops/mqtt2telegram
```
or run `../deploy-with-helm.sh` script