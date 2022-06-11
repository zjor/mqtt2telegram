package com.github.zjor.telegram;

import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.function.Consumer;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class MqttClient {

    private final String user;
    private final String password;
    private final Mqtt5BlockingClient mqttClient;

    @Inject
    public MqttClient(String host, int port, String user, String password) {
        this.mqttClient = buildClient(host, port);
        this.user = user;
        this.password = password;
    }

    private Mqtt5BlockingClient buildClient(String host, int port) {
        return com.hivemq.client.mqtt.MqttClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(port)
                .sslWithDefaultConfig()
                .addConnectedListener(ctx -> log.info("Connected to MQTT"))
                .addDisconnectedListener(ctx -> log.info("Disconnected from MQTT: {}", ctx.getSource()))
                .buildBlocking();
    }

    public boolean isConnected() {
        return mqttClient.getState() == MqttClientState.CONNECTED;
    }

    public void connect() {
        mqttClient.connectWith()
                .simpleAuth()
                .username(user)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send();
    }

    private void ensureConnected() {
        if (!isConnected()) {
            connect();
        }
    }

    public void setPublishListener(Consumer<Mqtt5Publish> consumer) {
        ensureConnected();
        mqttClient.toAsync().publishes(ALL, consumer);
    }

    public void subscribe(String topic) {
        ensureConnected();
        mqttClient.subscribeWith()
                .topicFilter(topic)
                .send();
    }

    public void unsubscribe(String topic) {
        ensureConnected();
        mqttClient.unsubscribeWith().topicFilter(topic).send();
    }

    public void publish(String topic, String payload) {
        ensureConnected();
        mqttClient.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(payload.getBytes())
                .send();
    }

}
