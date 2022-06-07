package com.github.zjor.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zjor.services.users.User;
import com.google.inject.Inject;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiSecurity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Slf4j
public class Rest2MqttController {

    private final ObjectMapper mapper;
    private final Mqtt5BlockingClient mqttClient;

    @Inject
    public Rest2MqttController(
            ObjectMapper mapper,
            Mqtt5BlockingClient mqttClient) {
        this.mapper = mapper;
        this.mqttClient = mqttClient;
    }

    @OpenApi(
            summary = "Sends message to MQTT broker to the topic provided in the request",
            security = @OpenApiSecurity(name = "basicAuth"),
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = SendMessageRequest.class)})
    )
    public void sendGlobally(@NotNull Context ctx) throws Exception {
        var req = mapper.readValue(ctx.bodyAsInputStream(), SendMessageRequest.class);
        log.info("Request: {}", req);
        if (mqttClient.getState() != MqttClientState.CONNECTED) {
            log.warn("MQTT client not connected");
            ctx.status(HttpCode.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("success", false, "mqttStatus", mqttClient.getState()));
        } else {
            mqttClient.publishWith()
                    .topic(req.getTopic())
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(req.getPayload().getBytes())
                    .send();
            ctx.json(Map.of("success", true));
        }
    }

    @OpenApi(
            summary = "Sends message to MQTT broker to the topic the user is subscribed to",
            security = @OpenApiSecurity(name = "basicAuth"),
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = SendMessageRequest.class)})
    )
    public void sendToMyTopic(@NotNull Context ctx) throws Exception {
        var req = mapper.readValue(ctx.bodyAsInputStream(), SendMessageRequest.class);
        log.info("Request: {}", req);
        if (mqttClient.getState() != MqttClientState.CONNECTED) {
            log.warn("MQTT client not connected");
            ctx.status(HttpCode.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("success", false, "mqttStatus", mqttClient.getState()));
        } else {
            var user = (User)ctx.attribute("user");
            var topic = user.getTelegramId() + "/" + req.getTopic();
            mqttClient.publishWith()
                    .topic(topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(req.getPayload().getBytes())
                    .send();
            ctx.json(Map.of("success", true));
        }
    }

    @Data
    public static class SendMessageRequest {
        private String topic;
        private String payload;
    }
}