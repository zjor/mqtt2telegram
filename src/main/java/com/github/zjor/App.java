package com.github.zjor;

import com.github.zjor.config.ApplicationModule;
import com.github.zjor.config.EnvironmentModule;
import com.github.zjor.telegram.TelegramBotRunner;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class App {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(
                new EnvironmentModule(),
                new ApplicationModule()
        );

        var botRunner = injector.getInstance(TelegramBotRunner.class);
        botRunner.start();
    }
}
