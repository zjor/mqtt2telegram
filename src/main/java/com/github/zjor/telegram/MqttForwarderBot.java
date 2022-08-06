package com.github.zjor.telegram;

import com.github.zjor.services.sub.Subscription;
import com.github.zjor.services.sub.SubscriptionService;
import com.github.zjor.services.users.UserService;
import com.google.common.eventbus.Subscribe;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class MqttForwarderBot extends AbilityBot {

    private final String apiBaseUrl;

    private final MqttClient mqttClient;

    private final UserService userService;
    private final SubscriptionService subscriptionService;

    private final Long creatorId;

    @Inject
    public MqttForwarderBot(
            String token,
            String botUsername,
            String apiBaseUrl,
            MqttClient mqttClient,
            UserService userService,
            SubscriptionService subscriptionService,
            Long creatorId) {
        super(token, botUsername);
        this.apiBaseUrl = apiBaseUrl;
        this.mqttClient = mqttClient;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.creatorId = creatorId;
    }

    public void init() {
        mqttClient.connect();
    }

    @Subscribe
    public void onConnectedToMqtt(MqttClient.MqttConnectedEvent e) {
        mqttClient.setPublishListener(this::onMessage);
    }

    private void onMessage(Mqtt5Publish msg) {
        try {
            log.info("[Message received] topic: {}; payload size: {}",
                    msg.getTopic(),
                    msg.getPayload().map(buf -> buf.remaining()).orElse(0));

            var levels = msg.getTopic().getLevels();
            var chatId = Long.valueOf(levels.get(0));
            var topic = levels.subList(1, levels.size()).stream().collect(Collectors.joining("/"));

            var contentType = msg.getContentType().orElse(MqttUtf8String.of("text")).toString();
            if (contentType.startsWith("image")) {
                var sendPhoto = SendPhoto.builder()
                        .chatId(String.valueOf(chatId))
                        .photo(new InputFile(new ByteArrayInputStream(msg.getPayloadAsBytes()), "image.png"))
                        .build();
                sender.sendPhoto(sendPhoto);
            } else {
                var message = "`[" + topic + "]`\n" +
                        UTF_8.decode(msg.getPayload().get());

                silent.sendMd(message, chatId);
            }
        } catch (Throwable t) {
            log.error("Failed to send telegram message: " + t.getMessage(), t);
        }
    }

    private String subscribe(Long userId, String topic) {
        var fullTopicName = userId + "/" + topic;
        mqttClient.subscribe(fullTopicName);
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
                .action(this::subscribeAbilityHandler)
                .build();
    }

    private void subscribeAbilityHandler(MessageContext ctx) {
        var u = ensureUserExists(ctx);
        String topic = ctx.firstArg();
        var fullTopicName = subscribe(ctx.chatId(), topic);

        var message = new StringBuilder("Subscribed to `" + fullTopicName + "`\n");
        message.append("This is how to send messages to the topic:\n");
        message.append(mqttSendCommandExample(u, topic, "<message>"));
        silent.sendMd(message.toString(), ctx.chatId());
    }

    @SuppressWarnings("unused")
    public Ability listSubscriptionsAbility() {
        return Ability.builder()
                .name("list")
                .input(0)
                .info("Lists my subscriptions")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(this::listSubscriptionsAbilityHandler)
                .build();
    }

    private void listSubscriptionsAbilityHandler(MessageContext ctx) {
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
    }

    @SuppressWarnings("unused")
    public Ability unsubscribeAbility() {
        return Ability.builder()
                .name("unsub")
                .input(1)
                .info("Unsubscribes from the topic. Usage: /unsub <topic>")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(this::unsubscribeAbilityHandler)
                .build();
    }

    private void unsubscribeAbilityHandler(MessageContext ctx) {
        ensureUserExists(ctx);
        String topic = ctx.firstArg();
        var fullTopicName = ctx.chatId() + "/" + topic;
        mqttClient.unsubscribe(fullTopicName);
        subscriptionService.unsubscribe(String.valueOf(ctx.chatId()), topic)
                .ifPresentOrElse(
                        sub -> silent.sendMd("Unsubscribed from: `" + sub.getTopic() + "`", ctx.chatId()),
                        () -> silent.send("Topic was not found", ctx.chatId()));
    }

    @SuppressWarnings("unused")
    public Ability showCredentialsAbility() {
        return Ability.builder()
                .name("creds")
                .input(0)
                .info("Shows credentials for basic authentication for the API calls")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(this::showCredentialsAbilityHandler)
                .build();
    }

    private void showCredentialsAbilityHandler(MessageContext ctx) {
        var u = ensureUserExists(ctx);
        var text = new StringBuilder("`")
                .append(u.getTelegramId()).append(":")
                .append(u.getSecret()).append("`");
        silent.sendMd(text.toString(), ctx.chatId());
    }

    private String resolveFirstName(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getFirstName();
        } else {
            return "Dear User";
        }
    }

    private String mqttSendCommandExample(com.github.zjor.services.users.User user, String topic, String message) {
        var text = new StringBuilder("\n```\n");
        text.append("http -a ").append(user.getTelegramId()).append(":");
        text.append(user.getSecret()).append(' ').append(apiBaseUrl).append("/api/v1.0/send ");
        text.append("topic=").append(topic).append(' ');
        text.append("payload='").append(message).append("'\n```\n");
        return text.toString();
    }

    @SuppressWarnings("unused")
    public Ability startAbility() {
        return Ability.builder()
                .name("start")
                .input(0)
                .info("Shows welcome message")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(this::startAbilityHandler)
                .build();
    }

    private void startAbilityHandler(MessageContext ctx) {
        var u = ensureUserExists(ctx);
        var msg = new StringBuilder("Hello, ").append(resolveFirstName(ctx.update())).append("!\n\n");
        msg.append("I'm Mqtt2TelegramBot, I can subscribe to MQTT topics and forward messages to you.\n");
        msg.append("Please use `/commands` to see the list of available commands\n\n");
        msg.append("Use this command to send a message to the topic you are subscribed to\n");
        msg.append(mqttSendCommandExample(u, "<topic>", "<message>"));
        msg.append("Happy messaging!\n\n");
        msg.append("P.S. You might need to install [httpie](https://httpie.io/).\n");
        msg.append("P.P.S. Fork me on [github](https://github.com/zjor/mqtt2telegram). ;)\n");

        SendMessage sendMessage = SendMessage.builder()
                .chatId(ctx.chatId().toString())
                .text(msg.toString())
                .build();
        sendMessage.enableMarkdown(true);
        sendMessage.disableWebPagePreview();
        silent.execute(sendMessage);
    }

    @SuppressWarnings("unused")
    public Ability adminStatsAbility() {
        return Ability.builder()
                .name("_stats")
                .input(0)
                .info("Shows statistics")
                .locality(Locality.ALL)
                .privacy(Privacy.CREATOR)
                .action(this::adminStatsAbilityHandler)
                .build();
    }

    private void adminStatsAbilityHandler(MessageContext ctx) {
        var msg = new StringBuilder("\nUsers: `").append(userService.count()).append('`');
        msg.append("\nSubscriptions: `").append(subscriptionService.count()).append("`");
        silent.sendMd(msg.toString(), ctx.chatId());
    }

    @SuppressWarnings("unused")
    public Ability httpieHelpAbility() {
        return Ability.builder()
                .name("httpie")
                .input(1)
                .info("Shows example command how to send a message via API")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(this::httpieHelpAbilityHandler)
                .build();
    }

    private void httpieHelpAbilityHandler(MessageContext ctx) {
        var topic = ctx.firstArg();
        var msg = new StringBuilder("This is how you can send a message to the topic: `").append(topic).append("`\n");
        var user = ensureUserExists(ctx);
        msg.append(mqttSendCommandExample(user, topic, "<your message>"));
        silent.sendMd(msg.toString(), ctx.chatId());
    }

    @SuppressWarnings("unused")
    public Ability pingAbility() {
        return Ability.builder()
                .name("ping")
                .info("Sends message to myself via MQTT. Usage: <topic> <message>")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(this::pingAbilityHandler)
                .build();
    }

    private void pingAbilityHandler(MessageContext ctx) {
        var args = ctx.arguments();
        if (args == null || args.length == 0) {
            silent.sendMd("Usage: <topic> <message>", ctx.chatId());
        } else {
            var topic = ctx.chatId() + "/" + args[0];
            var message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
            mqttClient.publish(topic, message);
        }
    }

    @SuppressWarnings("unused")
    public Ability changeCredentialsAbility() {
        return Ability.builder()
                .name("change")
                .info("Generates new credentials for the API or sets from the argument")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(this::changeCredentialsHandler)
                .build();
    }

    private void changeCredentialsHandler(MessageContext ctx) {
        var user = ensureUserExists(ctx);
        var newSecret = ctx.arguments() != null && ctx.arguments().length > 0 ? ctx.arguments()[0] : null;
        var updatedUser = userService.updateSecret(user.getTelegramId(), Optional.ofNullable(newSecret));
        var text = new StringBuilder("`")
                .append(updatedUser.getTelegramId()).append(":")
                .append(updatedUser.getSecret()).append("`");
        silent.sendMd(text.toString(), ctx.chatId());
    }

    @Override
    public long creatorId() {
        return creatorId;
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
