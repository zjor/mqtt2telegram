package com.github.zjor.telegram;

import com.github.zjor.services.sub.Subscription;
import com.github.zjor.services.sub.SubscriptionService;
import com.github.zjor.services.users.UserService;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class MqttForwarderBot extends AbilityBot {

    private final Mqtt5BlockingClient mqttClient;
    private final String mqttUser;
    private final String mqttPassword;

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Inject
    public MqttForwarderBot(
            String token,
            String botUsername,
            String mqttUser,
            String mqttPassword,
            Mqtt5BlockingClient mqttClient,
            UserService userService,
            SubscriptionService subscriptionService) {
        super(token, botUsername);
        this.mqttUser = mqttUser;
        this.mqttPassword = mqttPassword;
        this.mqttClient = mqttClient;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    public void init() {
        mqttClient.connectWith()
                .simpleAuth()
                .username(mqttUser)
                .password(UTF_8.encode(mqttPassword))
                .applySimpleAuth()
                .send();

        restoreSubscriptions();

        mqttClient.toAsync().publishes(ALL, msg -> {
            log.info("[Message received] topic: {}; payload size: {}",
                    msg.getTopic(),
                    msg.getPayload().map(buf -> buf.remaining()).orElse(0));

            var levels = msg.getTopic().getLevels();
            var chatId = levels.get(0);
            var topic = levels.subList(1, levels.size()).stream().collect(Collectors.joining("/"));
            var message = "`[" + topic + "]`\n" +
                    UTF_8.decode(msg.getPayload().get());
            silent.sendMd(message, Long.valueOf(chatId));
        });
    }

    private void restoreSubscriptions() {
        subscriptionService.getAllSubscriptions().forEach(sub -> {
            var fullTopicName = sub.getUserId() + "/" + sub.getTopic();
            mqttClient.subscribeWith()
                    .topicFilter(fullTopicName)
                    .send();
            log.info("Subscribed to {}", fullTopicName);
        });
    }

    private String subscribe(Long userId, String topic) {
        var fullTopicName = userId + "/" + topic;
        mqttClient.subscribeWith()
                .topicFilter(fullTopicName)
                .send();
        subscriptionService.subscribe(String.valueOf(userId), topic);
        return fullTopicName;
    }

    @SuppressWarnings("unused")
    public Ability subscribeAbility() {
        return Ability.builder()
                .name("sub")
                .input(1)
                .info("Subscribes to a topic. Usage: /sub <topic>")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    ensureUserExists(ctx);
                    String topic = ctx.firstArg();
                    var fullTopicName = subscribe(ctx.chatId(), topic);
                    silent.sendMd("Subscribed to `" + fullTopicName + "`", ctx.chatId());
                })
                .build();
    }

    @SuppressWarnings("unused")
    public Ability listSubscriptionsAbility() {
        return Ability.builder()
                .name("list")
                .input(0)
                .info("Lists my subscriptions")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    ensureUserExists(ctx);
                    List<Subscription> subs = subscriptionService.getMySubscriptions(String.valueOf(ctx.chatId()));
                    if (subs.isEmpty()) {
                        silent.send("No subscriptions", ctx.chatId());
                    } else {
                        StringBuilder msg = new StringBuilder("```\n");
                        subs.forEach(s -> msg.append("- ").append(s.getTopic()).append('\n'));
                        msg.append("```");
                        silent.sendMd(msg.toString(), ctx.chatId());
                    }
                })
                .build();
    }

    @SuppressWarnings("unused")
    public Ability unsubscribeAbility() {
        return Ability.builder()
                .name("unsub")
                .input(1)
                .info("Unsubscribes from the topic. Usage: /unsub <topic>")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    ensureUserExists(ctx);
                    String topic = ctx.firstArg();
                    var fullTopicName = ctx.chatId() + "/" + topic;
                    mqttClient.unsubscribeWith().topicFilter(fullTopicName);
                    subscriptionService.unsubscribe(String.valueOf(ctx.chatId()), topic)
                            .ifPresentOrElse(
                                    sub -> silent.sendMd("Unsubscribed from: `" + sub.getTopic() + "`", ctx.chatId()),
                                    () -> silent.send("Topic was not found", ctx.chatId()));
                })
                .build();
    }

    @SuppressWarnings("unused")
    public Ability showCredsAbility() {
        return Ability.builder()
                .name("creds")
                .input(0)
                .info("Shows credentials for basic authentication for the API calls")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    var u = ensureUserExists(ctx);
                    var text = new StringBuilder("`")
                            .append(u.getTelegramId()).append(":")
                            .append(u.getSecret()).append("`");
                    silent.sendMd(text.toString(), ctx.chatId());
                })
                .build();
    }

    private String resolveFirstName(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getFirstName();
        } else {
            return "Dear User";
        }
    }

    @SuppressWarnings("unused")
    public Ability startAbility() {
        return Ability.builder()
                .name("start")
                .input(0)
                .info("Shows welcome message")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    ensureUserExists(ctx);
                    var msg = new StringBuilder("Hello, ").append(resolveFirstName(ctx.update())).append("!\n");
                    msg.append("I'm Mqtt2TelegramBot, I can subscribe to MQTT topics and forward messages to you.\n");
                    msg.append("Please use `/commands` to see the list of available commands\n\n");
                    msg.append("Happy messaging!");
                    silent.sendMd(msg.toString(), ctx.chatId());
                })
                .build();
    }


        @Override
    public long creatorId() {
        return 79079907;
    }

    private com.github.zjor.services.users.User ensureUserExists(MessageContext ctx) {
        User from = null;
        if (ctx.update().hasMessage()) {
            from = ctx.update().getMessage().getFrom();
        } else if (ctx.update().hasEditedMessage()) {
            from = ctx.update().getEditedMessage().getFrom();
        }
        if (from != null) {
            return userService.ensureExists(from);
        } else {
            throw new IllegalArgumentException("Telegram user is not set");
        }
    }
}
