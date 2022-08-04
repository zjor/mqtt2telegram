package com.github.zjor.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zjor.services.users.User;
import com.github.zjor.telegram.MqttClient;
import com.google.inject.Inject;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiFileUpload;
import io.javalin.plugin.openapi.annotations.OpenApiFormParam;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiSecurity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Map;

@Slf4j
public class Rest2MqttController {

    private static final Map<String, Object> DEFAULT_OK = Map.of("success", true);

    private final ObjectMapper mapper;
    private final MqttClient mqttClient;
    private final Long creatorTelegramId;

    @Inject
    public Rest2MqttController(
            ObjectMapper mapper,
            MqttClient mqttClient,
            Long creatorTelegramId) {
        this.mapper = mapper;
        this.mqttClient = mqttClient;
        this.creatorTelegramId = creatorTelegramId;
    }

    @OpenApi(
            summary = "Sends message to MQTT broker to the topic provided in the request",
            security = @OpenApiSecurity(name = "basicAuth"),
            requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = SendMessageRequest.class)})
    )
    public void sendGlobally(@NotNull Context ctx) throws Exception {
        var req = mapper.readValue(ctx.bodyAsInputStream(), SendMessageRequest.class);
        log.info("Request: {}", req);
        if (!mqttClient.isConnected()) {
            log.warn("MQTT client not connected");
            error(ctx, HttpCode.INTERNAL_SERVER_ERROR, "MQTT client is not connected");
        } else {
            mqttClient.publish(req.getTopic(), req.getPayload());
            ctx.json(DEFAULT_OK);
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
        if (!mqttClient.isConnected()) {
            log.warn("MQTT client not connected");
            error(ctx, HttpCode.INTERNAL_SERVER_ERROR, "MQTT client is not connected");
        } else {
            var user = (User) ctx.attribute("user");
            var topic = user.getTelegramId() + "/" + req.getTopic();
            mqttClient.publish(topic, req.getPayload());
            ctx.json(DEFAULT_OK);
        }
    }

    @OpenApi(
            path = "/api/v1.0/sendImage",
            method = HttpMethod.POST,
            summary = "Sends an image to MQTT",
            formParams = {
                    @OpenApiFormParam(name = "topic", required = true),
            },
            fileUploads = {
                    @OpenApiFileUpload(name="image")
            }
    )
    public void sendImageToMyTopic(@NotNull Context ctx) throws Exception {
        var topic = ctx.formParam("topic");
        if (StringUtils.isEmpty(topic)) {
            error(ctx, HttpCode.BAD_REQUEST, "topic is empty");
            return;
        }

        var files = ctx.uploadedFiles();
        if (files.isEmpty()) {
            error(ctx, HttpCode.BAD_REQUEST, "no image");
            return;
        }
        var user = (User) ctx.attribute("user");
        var image = files.get(0);
        var imageContent = image.getContent();

        mqttClient.publish(
                user.getTelegramId() + "/" + topic,
                image.getFilename(),
                ByteBuffer.wrap(imageContent.readAllBytes()));

        ctx.json(DEFAULT_OK);
    }

    public void adminMqttConnect(@NotNull Context ctx) {
        var user = (User)ctx.attribute("user");
        if (user.getTelegramId() == creatorTelegramId) {
            mqttClient.ensureConnected();
            ctx.json(DEFAULT_OK);
        } else {
            error(ctx, HttpCode.UNAUTHORIZED, "Not a creator");
        }
    }

    public void adminMqttDisconnect(@NotNull Context ctx) {
        var user = (User)ctx.attribute("user");
        if (user.getTelegramId() == creatorTelegramId) {
            mqttClient.disconnect();
            ctx.json(DEFAULT_OK);
        } else {
            error(ctx, HttpCode.UNAUTHORIZED, "Not a creator");
        }
    }

    public static void error(@NotNull Context ctx, HttpCode code, String message) {
        ctx.status(code);
        ctx.json(Map.of("success", false, "message", message));
    }

    @Data
    public static class SendMessageRequest {
        private String topic;
        private String payload;
    }
}
