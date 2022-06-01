package com.github.zjor.telegram;

import com.google.common.base.Strings;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;

import javax.inject.Inject;
import java.util.stream.Collectors;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MqttForwarderBot extends AbilityBot {

    private final Mqtt5BlockingClient mqttClient;
    private final String mqttUser;
    private final String mqttPassword;

    @Inject
    public MqttForwarderBot(
            String token,
            String botUsername,
            String mqttUser,
            String mqttPassword,
            Mqtt5BlockingClient mqttClient) {
        super(token, botUsername);
        this.mqttUser = mqttUser;
        this.mqttPassword = mqttPassword;
        this.mqttClient = mqttClient;
    }

    public void init() {
        mqttClient.connectWith()
                .simpleAuth()
                .username(mqttUser)
                .password(UTF_8.encode(mqttPassword))
                .applySimpleAuth()
                .send();

        mqttClient.toAsync().publishes(ALL, publish -> {
            var levels = publish.getTopic().getLevels();
            var chatId = levels.get(0);
            var topic = levels.subList(1, levels.size()).stream().collect(Collectors.joining("/"));
            var message = "`[" + topic + "]`\n" +
                    UTF_8.decode(publish.getPayload().get());
            silent.sendMd(message, Long.valueOf(chatId));
        });
    }

    @SuppressWarnings("unused")
    public Ability subscribeAbility() {
        return Ability.builder()
                .name("sub")
                .info("Subscribes to a topic")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    String topic = ctx.firstArg();
                    if (Strings.isNullOrEmpty(topic)) {
                        silent.send("Topic is empty, please repeat the command", ctx.chatId());
                    } else {
                        var fullTopicName = ctx.chatId() + "/" + topic;
                        mqttClient.subscribeWith()
                                .topicFilter(fullTopicName)
                                .send();
                        silent.send("Subscribed to " + fullTopicName, ctx.chatId());
                    }
                })
                .build();
    }

    @Override
    public long creatorId() {
        return 79079907;
    }
}
