![logo](logo.png)
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
import requests

url = "https://mqtt2telegram.projects.royz.cc/api/v1.0/send"

login = "1234567"
password = "aaBBccEEdd"

topic = "YOUR_TOPIC"

json = {
    "topic": topic,
    "payload": "YOUR_MESSAGE"
}

requests.post(url, auth=(login, password), json=json)
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
import got from "got"

async function sendMessage(login, password, topic, message) {
  const url = 'https://mqtt2telegram.projects.royz.cc/api/v1.0/send'
  const hash = btoa(`${login}:${password}`)
  const options = {
    headers: {
      authorization: `Basic ${hash}`
    }

  }
  const json = {
    topic, 
    payload: message
  }
  return got.post(url, {...options, json})
}

async function main() {
  const login = '1234567'
  const password = 'aaBBccEEdd'
  const topic = 'topic'
  const message = 'Hello from JavaScript'
  
  const res = await sendMessage(login, password, topic, message)
  console.log(res)
}

main().catch(console.log)
```

## Features
- allows managing notifications types via the bot, e.g. `/sub`, `/list`, `/unsub`
- provides REST API for sending messages
- gives personal API key

## Use cases
- send messages on regular basis, e.g. weather, [quote of the day](https://github.com/zjor/automation)
- notify of events like CI/CD pipeline status or alerts
- send readings from IoT sensors 