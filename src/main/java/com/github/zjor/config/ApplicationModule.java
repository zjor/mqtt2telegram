package com.github.zjor.config;

import com.github.zjor.services.sub.SubscriptionService;
import com.github.zjor.services.users.UserService;
import com.github.zjor.telegram.MqttClient;
import com.github.zjor.telegram.MqttForwarderBot;
import com.github.zjor.telegram.TelegramBotRunner;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import javax.inject.Singleton;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        //TODO: to env
        bind(Long.class).annotatedWith(Names.named("creatorId")).toInstance(79079907L);
        bind(MqttClient.class).asEagerSingleton();
    }

    @Inject
    @Provides
    @Singleton
    public MqttForwarderBot mqttForwarderBot(
            @Named(EnvironmentModule.TELEGRAM_TOKEN) String token,
            @Named(EnvironmentModule.TELEGRAM_BOT_USERNAME) String botUsername,
            @Named(EnvironmentModule.API_BASE_URL) String apiBaseUrl,
            MqttClient mqttClient,
            UserService userService,
            SubscriptionService subscriptionService,
            @Named("creatorId") Long creatorId) {
        return new MqttForwarderBot(token,
                botUsername,
                apiBaseUrl,
                mqttClient,
                userService,
                subscriptionService, creatorId);
    }

    @Inject
    @Provides
    @Singleton
    public TelegramBotRunner telegramBotRunner(
            MqttForwarderBot bot,
            @Named(EnvironmentModule.VCS_REF) String vcsRef) {
        return new TelegramBotRunner(bot, vcsRef);
    }

    @Provides
    @Singleton
    protected MongoClient mongoClient(
            @Named(EnvironmentModule.MONGO_URI) String uri) {
        return new MongoClient(new MongoClientURI(uri));
    }

    @Inject
    @Provides
    @Singleton
    public SubscriptionService subscriptionService(MongoClient mongoClient) {
        return new SubscriptionService(mongoClient);
    }

    @Inject
    @Provides
    @Singleton
    public UserService userService(MongoClient mongoClient) {
        return new UserService(mongoClient);
    }
}
