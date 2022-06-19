# Enable telegram notifications for your project in less than a minute!

## Getting started
1. Say `/start` to the [bot](https://t.me/Mqtt2TelegramBot)
2. Subscribe to a topic, e.g. *weather* using a command `/sub weather`
3. Use a CLI command from the response, e.g.:
```bash
http -a 1234567:aaBBccDDee https://mqtt2telegram.projects.royz.cc/api/v1.0/send topic=weather payload='<your message>'
```
or one of the snippets of code below.

## Code snippets

### Python

```python
import json
import requests

url = "https://mqtt2telegram.projects.royz.cc/api/v1.0/send"

login = "1234567"
password = "aaBBccEEdd"

topic = "YOUR_TOPIC"

data = {
    "topic": topic,
    "payload": "YOUR_MESSAGE"
}

requests.post(url, auth=(login, password), data=json.dumps(data))
```

### cURL

```bash
LOGIN=1234567 \
PASSWORD=aaBBccEEdd \
TOPIC=topic \
MESSAGE="hello world" \
curl -v -X POST -u "${LOGIN}:${PASSWORD}" \
https://mqtt2telegram.projects.royz.cc/api/v1.0/send \
-H "Content-Type: application/json" \
-d "{\"topic\": \"${TOPIC}\", \"payload\": \"${MESSAGE}\"}"
```

### JavaScript

```javascript

// coming soon
```

## Features
- allows managing notifications types via the bot, e.g. `/sub`, `/list`, `/unsub`
- provides REST API for sending messages
- gives personal API key

## Use cases
- send messages on regular basis, e.g. weather, [quote of the day](https://github.com/zjor/automation)
- notify of events like CI/CD pipeline status or alerts
- send readings from IoT sensors 