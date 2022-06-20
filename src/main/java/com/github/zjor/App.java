package com.github.zjor;

import com.github.zjor.config.ApplicationModule;
import com.github.zjor.config.EnvironmentModule;
import com.github.zjor.config.JavalinModule;
import com.github.zjor.ext.guice.LoggingModule;
import com.github.zjor.telegram.RestoreSubscriptionsJob;
import com.github.zjor.telegram.TelegramBotRunner;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(
                new LoggingModule(),
                new EnvironmentModule(),
                new ApplicationModule(),
                new JavalinModule()
        );

        var eventBus = injector.getInstance(EventBus.class);
        eventBus.register(injector.getInstance(RestoreSubscriptionsJob.class));

        var botRunner = injector.getInstance(TelegramBotRunner.class);
        botRunner.start();

        var server = injector.getInstance(Javalin.class);
        server.start(8080);

        botRunner.sendDeployedMessage();
    }
}
