package com.github.zjor.telegram;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class TelegramBotRunner {

    private final MqttForwarderBot bot;

    @Inject
    public TelegramBotRunner(MqttForwarderBot bot) {
        this.bot = bot;
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

}
