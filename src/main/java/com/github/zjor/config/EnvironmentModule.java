package com.github.zjor.config;


import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class EnvironmentModule extends AbstractModule {

    public static final String MQTT_HOST = "MQTT_HOST";
    public static final String MQTT_PORT = "MQTT_PORT";
    public static final String MQTT_USER = "MQTT_USER";
    public static final String MQTT_PASSWORD = "MQTT_PASSWORD";
    public static final String TELEGRAM_TOKEN = "TELEGRAM_TOKEN";
    public static final String TELEGRAM_BOT_USERNAME = "TELEGRAM_BOT_USERNAME";
    public static final String MONGO_URI = "MONGO_URI";
    public static final String API_BASE_URL = "API_BASE_URL";

    private static final List<String> ALL_NAMES = Arrays.asList(
            MQTT_HOST,
            MQTT_PORT,
            MQTT_USER,
            MQTT_PASSWORD,
            TELEGRAM_TOKEN,
            TELEGRAM_BOT_USERNAME,
            MONGO_URI,
            API_BASE_URL
    );

    @Override
    protected void configure() {
        for (String name : ALL_NAMES) {
            var value = checkNotNull(System.getenv(name), "Variable: " + name + " is not set");
            bind(String.class).annotatedWith(Names.named(name)).toInstance(value);
        }
    }
}
