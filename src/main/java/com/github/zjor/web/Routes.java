package com.github.zjor.web;

import com.github.zjor.config.EnvironmentModule;
import com.github.zjor.web.validator.ContentTypeValidator;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Routes implements EndpointGroup {

    private final Rest2MqttController rest2MqttController;
    private final String vcsRef;

    @Inject
    public Routes(
            Rest2MqttController rest2MqttController,
            @Named(EnvironmentModule.VCS_REF) String vcsRef) {
        this.rest2MqttController = rest2MqttController;
        this.vcsRef = vcsRef;
    }

    @Override
    public void addEndpoints() {
        get("/", ctx -> ctx.html("OK"), Role.ANYONE);
        get("/version", this::versionHandler, Role.ANYONE);
        post("/api/v1.0/sendGlobally", ContentTypeValidator.json(rest2MqttController::sendGlobally), Role.AUTHENTICATED);
        post("/api/v1.0/send", ContentTypeValidator.json(rest2MqttController::sendToMyTopic), Role.AUTHENTICATED);
        post("/api/v1.0/sendImage", rest2MqttController::sendImageToMyTopic, Role.AUTHENTICATED);
        post("/api/v1.0/admin/mqtt/connect", rest2MqttController::adminMqttConnect, Role.AUTHENTICATED);
        post("/api/v1.0/admin/mqtt/disconnect", rest2MqttController::adminMqttDisconnect, Role.AUTHENTICATED);
    }

    private void versionHandler(Context ctx) {
        ctx.json(Map.of("vcsRef", vcsRef));
    }
}
