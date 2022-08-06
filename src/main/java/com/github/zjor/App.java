package com.github.zjor;

import com.github.zjor.config.ApplicationModule;
import com.github.zjor.config.EnvironmentModule;
import com.github.zjor.config.JavalinModule;
import com.github.zjor.ext.guice.LoggingModule;
import com.github.zjor.telegram.MqttForwarderBot;
import com.github.zjor.telegram.RestoreSubscriptionsJob;
import com.github.zjor.telegram.TelegramBotRunner;
import com.github.zjor.telegram.TelegramEventSender;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

        Injector injector = Guice.createInjector(
                new LoggingModule(),
                new EnvironmentModule(),
                new ApplicationModule(),
                new JavalinModule()
        );

        var eventBus = injector.getInstance(EventBus.class);
        eventBus.register(injector.getInstance(RestoreSubscriptionsJob.class));
        eventBus.register(injector.getInstance(TelegramEventSender.class));
        eventBus.register(injector.getInstance(MqttForwarderBot.class));

        var botRunner = injector.getInstance(TelegramBotRunner.class);
        botRunner.start();

        var server = injector.getInstance(Javalin.class);
        server.start(8080);

        botRunner.sendDeployedMessage();
    }
}
