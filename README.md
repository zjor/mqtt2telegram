# MQTT 2 Telegram

## Functional requirements
1. User adds a telegram bot
2. User subscribes to a topic
3. When message is sent to a topic, it is forwarded to the user

## Implementation details

- Topic name: `${user_id}/${user_topic}`
- Subscription should be persisted in the database, so the subscription is restored when the service is restarted
- VM options: `-Djdk.tls.client.protocols=TLSv1.2`

## Telegram Bot commands
- `/subscribe {topic}` - subscribes to a topic
- `/list` - lists my subscriptions
- `/unsubscribe {topic}`

## CLI commands

### Send text message

```bash
source .env
mqtt pub -h ${MQTT_HOST} -p 8883 -s -u ${MQTT_USER} --password ${MQTT_PASSWORD} -t '79079907/binance' -m 'Hello world'
```

### Send an image

```bash
source .env
mqtt pub -h ${MQTT_HOST} -p 8883 -s -u ${MQTT_USER} --password ${MQTT_PASSWORD} -t '79079907/qotd' -m:file ~/tmp/img.jpeg -ct 'image:img.jpeg'
```

## Stack
- guice
- javalin
- telegramBots
- java mongo driver & atlas cloud

### REST API Authentication

Basic authorization of `telegramId:secret`

## Libraries
- [Telegram Bot API](https://github.com/rubenlagus/TelegramBots)
- [HiveMQ MQTT client](https://github.com/hivemq/hivemq-mqtt-client)

