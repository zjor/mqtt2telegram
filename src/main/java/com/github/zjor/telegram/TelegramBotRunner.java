package com.github.zjor.telegram;

import com.github.zjor.telegram.events.SendMessageToCreatorEvent;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class TelegramBotRunner {

    private final MqttForwarderBot bot;
    private final String vcsRef;

    private final EventBus eventBus;

    public TelegramBotRunner(MqttForwarderBot bot, String vcsRef, EventBus eventBus) {
        this.bot = bot;
        this.vcsRef = vcsRef;
        this.eventBus = eventBus;
    }

    public void start() {
        try {
            bot.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            log.info("started");
        } catch (TelegramApiException e) {
            log.error("Failed to start telegram bot: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void sendDeployedMessage() {
        eventBus.post(new SendMessageToCreatorEvent("Started version: `" + vcsRef + "`"));
    }

}
