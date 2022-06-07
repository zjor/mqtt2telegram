package com.github.zjor.web;

import com.google.inject.Inject;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Routes implements EndpointGroup {

    private final Rest2MqttController rest2MqttController;

    @Inject
    public Routes(Rest2MqttController rest2MqttController) {
        this.rest2MqttController = rest2MqttController;
    }

    @Override
    public void addEndpoints() {
        get("/", ctx -> ctx.html("OK"), Role.ANYONE);
        post("/api/v1.0/sendGlobally", rest2MqttController::sendGlobally, Role.AUTHENTICATED);
        post("/api/v1.0/send", rest2MqttController::sendToMyTopic, Role.AUTHENTICATED);
    }
}
