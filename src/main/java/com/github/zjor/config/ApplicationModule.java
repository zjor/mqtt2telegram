package com.github.zjor.config;

import com.github.zjor.services.sub.SubscriptionService;
import com.github.zjor.services.users.UserService;
import com.github.zjor.telegram.MqttClient;
import com.github.zjor.telegram.MqttForwarderBot;
import com.github.zjor.telegram.RestoreSubscriptionsJob;
import com.github.zjor.telegram.TelegramBotRunner;
import com.github.zjor.telegram.TelegramEventSender;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import javax.inject.Singleton;

public class ApplicationModule extends AbstractModule {

    public static final String MONGO_DB_NAME = "mongo.dbName";

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named(MONGO_DB_NAME)).toInstance("mqtt2telegram");
        bind(MqttClient.class).asEagerSingleton();
        bind(EventBus.class).asEagerSingleton();
        bind(TelegramEventSender.class).asEagerSingleton();
    }

    @Inject
    @Provides
    @Singleton
    public MqttForwarderBot mqttForwarderBot(
            @Named(EnvironmentModule.TELEGRAM_TOKEN) String token,
            @Named(EnvironmentModule.TELEGRAM_BOT_USERNAME) String botUsername,
            @Named(EnvironmentModule.API_BASE_URL) String apiBaseUrl,
            @Named(EnvironmentModule.TELEGRAM_USER_ID) String creatorId,
            MqttClient mqttClient,
            UserService userService,
            SubscriptionService subscriptionService) {
        return new MqttForwarderBot(token,
                botUsername,
                apiBaseUrl,
                mqttClient,
                userService,
                subscriptionService,
                Long.parseLong(creatorId));
    }

    @Inject
    @Provides
    @Singleton
    public TelegramBotRunner telegramBotRunner(
            MqttForwarderBot bot,
            @Named(EnvironmentModule.VCS_REF) String vcsRef,
            EventBus eventBus) {
        return new TelegramBotRunner(bot, vcsRef, eventBus);
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
    public SubscriptionService subscriptionService(
            MongoClient mongoClient,
            @Named(MONGO_DB_NAME) String dbName) {
        return new SubscriptionService(mongoClient, dbName);
    }

    @Inject
    @Provides
    @Singleton
    public UserService userService(
            MongoClient mongoClient,
            @Named(MONGO_DB_NAME) String dbName) {
        return new UserService(mongoClient, dbName);
    }

    @Inject
    @Provides
    @Singleton
    public RestoreSubscriptionsJob restoreSubscriptionsJob(
            SubscriptionService subscriptionService,
            MqttClient mqttClient) {
        return new RestoreSubscriptionsJob(subscriptionService, mqttClient);
    }
}
