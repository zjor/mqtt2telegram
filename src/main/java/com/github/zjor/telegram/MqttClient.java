package com.github.zjor.telegram;

import com.github.zjor.config.EnvironmentModule;
import com.github.zjor.ext.guice.Log;
import com.github.zjor.telegram.events.SendMessageToCreatorEvent;
import com.google.common.eventbus.EventBus;
import com.google.inject.name.Named;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class MqttClient {

    private final String user;
    private final String password;
    private final Mqtt5BlockingClient mqttClient;
    private final EventBus eventBus;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Inject
    public MqttClient(
            @Named(EnvironmentModule.MQTT_HOST) String host,
            @Named(EnvironmentModule.MQTT_PORT) String port,
            @Named(EnvironmentModule.MQTT_USER) String user,
            @Named(EnvironmentModule.MQTT_PASSWORD) String password,
            EventBus eventBus) {
        this.eventBus = eventBus;
        this.mqttClient = buildClient(host, Integer.valueOf(port));
        this.user = user;
        this.password = password;
    }

    private Mqtt5BlockingClient buildClient(String host, int port) {
        return com.hivemq.client.mqtt.MqttClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(port)
                .sslWithDefaultConfig()
                .addConnectedListener(ctx -> {
                    log.info("Connected to MQTT");
                    eventBus.post(new SendMessageToCreatorEvent("Connected to MQTT"));
                    eventBus.post(new MqttConnectedEvent(ctx));
                })
                .addDisconnectedListener(ctx -> {
                    log.info("Disconnected from MQTT: {}", ctx.getSource());
                    eventBus.post(new SendMessageToCreatorEvent("Disconnected from MQTT: " + ctx.getCause()));
                    scheduleReconnect();
                })
                .buildBlocking();
    }

    public boolean isConnected() {
        return mqttClient.getState().isConnected();
    }

    public void connect() {
        mqttClient.connectWith()
                .simpleAuth()
                .username(user)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send();
    }

    public void ensureConnected() {
        if (!isConnected()) {
            connect();
        }
    }

    public void setPublishListener(Consumer<Mqtt5Publish> consumer) {
        ensureConnected();
        mqttClient.toAsync().publishes(ALL, consumer);
    }

    @Log
    public void subscribe(String topic) {
        ensureConnected();
        mqttClient.subscribeWith()
                .topicFilter(topic)
                .send();
    }

    @Log
    public void unsubscribe(String topic) {
        ensureConnected();
        mqttClient.unsubscribeWith().topicFilter(topic).send();
    }

    @Log
    public void publish(String topic, String payload) {
        ensureConnected();
        mqttClient.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(payload.getBytes())
                .send();
    }

    public void publish(String topic, String filename, ByteBuffer payload) {
        ensureConnected();
        mqttClient.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .contentType("image:" + filename)
                .payload(payload)
                .send();
    }

    public void disconnect() {
        mqttClient.disconnect();
    }

    private void scheduleReconnect() {
        executorService.schedule(() -> {
            log.info("Reconnecting to MQTT...");
            if (mqttClient.getState().isConnectedOrReconnect()) {
                log.info("Already connected");
                return;
            }
            if (mqttClient.getState() == MqttClientState.DISCONNECTED) {
                connect();
            }
            scheduleReconnect();
        }, 3, TimeUnit.SECONDS);
    }

    @AllArgsConstructor
    public static class MqttConnectedEvent {
        public final MqttClientConnectedContext e;
    }

}
