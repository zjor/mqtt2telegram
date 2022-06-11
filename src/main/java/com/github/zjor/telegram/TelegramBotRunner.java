package com.github.zjor.telegram;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class TelegramBotRunner {

    private final MqttForwarderBot bot;
    private final String vcsRef;

    @Inject
    public TelegramBotRunner(MqttForwarderBot bot, String vcsRef) {
        this.bot = bot;
        this.vcsRef = vcsRef;
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
        bot.silent().sendMd("Started version: `" + vcsRef + "`", bot.creatorId());
    }

}
