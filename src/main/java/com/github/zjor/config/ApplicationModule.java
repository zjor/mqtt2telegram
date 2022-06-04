package com.github.zjor.config;

import com.github.zjor.services.sub.SubscriptionService;
import com.github.zjor.services.users.UserService;
import com.github.zjor.telegram.MqttForwarderBot;
import com.github.zjor.telegram.TelegramBotRunner;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import javax.inject.Singleton;

public class ApplicationModule extends AbstractModule {

    @Inject
    @Provides
    @Singleton
    public Mqtt5BlockingClient mqttClient(
            @Named(EnvironmentModule.MQTT_HOST) String host,
            @Named(EnvironmentModule.MQTT_PORT) String port) {
        return MqttClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(Integer.valueOf(port))
                .sslWithDefaultConfig()
                .buildBlocking();
    }

    @Inject
    @Provides
    @Singleton
    public MqttForwarderBot mqttForwarderBot(
            @Named(EnvironmentModule.TELEGRAM_TOKEN) String token,
            @Named(EnvironmentModule.TELEGRAM_BOT_USERNAME) String botUsername,
            @Named(EnvironmentModule.MQTT_USER) String mqttUser,
            @Named(EnvironmentModule.MQTT_PASSWORD) String mqttPassword,
            Mqtt5BlockingClient mqttClient,
            UserService userService,
            SubscriptionService subscriptionService) {
        return new MqttForwarderBot(token, botUsername, mqttUser, mqttPassword, mqttClient, userService, subscriptionService);
    }

    @Inject
    @Provides
    @Singleton
    public TelegramBotRunner telegramBotRunner(MqttForwarderBot bot) {
        return new TelegramBotRunner(bot);
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
