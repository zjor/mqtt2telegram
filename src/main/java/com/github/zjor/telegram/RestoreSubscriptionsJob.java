package com.github.zjor.telegram;

import com.github.zjor.services.sub.SubscriptionService;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RestoreSubscriptionsJob {

    private final SubscriptionService subscriptionService;
    private final MqttClient mqttClient;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    @Inject
    public RestoreSubscriptionsJob(
            SubscriptionService subscriptionService,
            MqttClient mqttClient) {
        this.subscriptionService = subscriptionService;
        this.mqttClient = mqttClient;
    }

    @Subscribe
    public void restoreSubscriptions(MqttClient.MqttConnectedEvent e) {
        executor.submit(() -> {
            log.info("Restoring subscriptions");
            subscriptionService.getAllSubscriptions().forEach(sub -> {
                var fullTopicName = sub.getUserId() + "/" + sub.getTopic();
                mqttClient.subscribe(fullTopicName);
            });
        });
    }

}
