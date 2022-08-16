package com.github.zjor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zjor.services.users.UserService;
import com.github.zjor.telegram.MqttClient;
import com.github.zjor.web.AccessManagerImpl;
import com.github.zjor.web.Rest2MqttController;
import com.github.zjor.web.Routes;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.plugin.OpenApiConfiguration;
import io.javalin.openapi.plugin.OpenApiInfo;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

public class JavalinModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Routes.class).asEagerSingleton();
    }

    private OpenApiConfiguration getOpenApiConfiguration() {
        var config = new OpenApiConfiguration();
        var info = new OpenApiInfo();
        info.setTitle("Mqtt2Telegram");
        info.setDescription("MQTT-to-Telegram Sender Bot");
        info.setVersion("1.0");
        config.setInfo(info);
        config.setDocumentationPath("open-api");
        return config;
    }

    @Inject
    @Provides
    @Singleton
    public Rest2MqttController rest2MqttController(
            ObjectMapper objectMapper,
            MqttClient mqttClient,
            @Named(EnvironmentModule.TELEGRAM_USER_ID) String creatorId) {
        return new Rest2MqttController(objectMapper, mqttClient, Long.parseLong(creatorId));
    }

    @Inject
    @Provides
    @Singleton
    private AccessManagerImpl accessManager(UserService userService) {
        return new AccessManagerImpl(userService);
    }

    @Inject
    @Provides
    @Singleton
    protected Javalin javalin(Routes routes, AccessManagerImpl accessManager) {
        var app = Javalin.create(config -> {
            config.plugins.register(new OpenApiPlugin(getOpenApiConfiguration()));

            var swaggerConfig = new SwaggerConfiguration();
            swaggerConfig.setDocumentationPath("/swagger");
            config.plugins.register(new SwaggerPlugin(swaggerConfig));

            config.core.accessManager(accessManager);
            config.http.maxRequestSize = 25_000_000L;

        });

        app.routes(routes);
        app.exception(Exception.class,
                (e, ctx) -> Rest2MqttController.error(ctx, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));

        return app;
    }
}
