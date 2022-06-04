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

```bash
source .env
mqtt pub -h ${MQTT_HOST} -p 8883 -s -u ${MQTT_USER} --password ${MQTT_PASSWORD} -t '79079907/binance' -m 'Hello'
```

## Stack
- guice
- javalin
- telegramBots
- java mongo driver & atlas cloud

### TODO
- [v] hide all creds to .env / config & gitignore
- [v] configure persistence => atlas mongoDB
- [v] store subscriptions during service restart
- [v] list my subscriptions
- [v] support unsub
- [v] deploy to my cluster
- create users on `/start` with names and secret
- provide credentials to subscribed users to use REST API
- add rest API to mqtt with Javalin
- rotate creds command
- ask for an example `curl` command
- restore subscriptions asynchronously
- [v] add bot self-documentation
- on subscribe show `curl` example show to send a test message
- send test message to itself
- github pages
- opensource
- deploy with gitlab actions
- publish articles (medium, habr, arduino)

## Libraries
- [Telegram Bot API](https://github.com/rubenlagus/TelegramBots)
- [HiveMQ MQTT client](https://github.com/hivemq/hivemq-mqtt-client)

