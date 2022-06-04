package com.github.zjor.web;

import com.google.inject.Inject;
import io.javalin.apibuilder.EndpointGroup;
import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes implements EndpointGroup {

    private final Rest2MqttHandler rest2MqttHandler;

    @Inject
    public Routes(Rest2MqttHandler rest2MqttHandler) {
        this.rest2MqttHandler = rest2MqttHandler;
    }

    @Override
    public void addEndpoints() {
        get("/", ctx -> ctx.html("OK"));
        post("/api/v1.0/send", rest2MqttHandler);
    }
}
