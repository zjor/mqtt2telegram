package com.github.zjor.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class Rest2MqttHandler implements Handler {

    private final ObjectMapper mapper;

    @Inject
    public Rest2MqttHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        var req = mapper.readValue(ctx.bodyAsInputStream(), SendMessageRequest.class);
        log.info("Request: {}", req);
//        ctx.json(req);
        ctx.json("hello");
    }

    @Data
    public static class SendMessageRequest {
        private String topic;
        private String payload;
    }
}
