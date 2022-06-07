package com.github.zjor.config;

import com.github.zjor.services.users.UserService;
import com.github.zjor.web.AccessManagerImpl;
import com.github.zjor.web.Rest2MqttHandler;
import com.github.zjor.web.Role;
import com.github.zjor.web.Routes;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;

public class JavalinModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Rest2MqttHandler.class).asEagerSingleton();
        bind(Routes.class).asEagerSingleton();
    }

    private OpenApiOptions getOpenApiOptions() {
        final Info applicationInfo = new Info()
                .version("1.0")
                .description("MQTT-to-Telegram Sender");
        return new OpenApiOptions(applicationInfo)
                .path("/swagger-docs")
                .swagger(new SwaggerOptions("/swagger").title("Mqtt2Telegram :: Swagger"))
                .reDoc(new ReDocOptions("/redoc").title("Mqtt2Telegram :: ReDoc"))
                .roles(Role.ANYONE);
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
            config.registerPlugin(new OpenApiPlugin(getOpenApiOptions()));
            config.accessManager(accessManager);
        });

        app.routes(routes);
        return app;
    }
}
