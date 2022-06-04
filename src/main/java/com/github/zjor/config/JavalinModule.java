package com.github.zjor.config;

import com.github.zjor.web.Routes;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.javalin.Javalin;

public class JavalinModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Routes.class).asEagerSingleton();
    }

    @Inject
    @Provides
    @Singleton
    protected Javalin javalin(Routes routes) {
        var app = Javalin.create();
        app.routes(routes);
        return app;
    }
}
