package com.github.zjor.telegram;

import com.github.zjor.telegram.events.SendMessageEvent;
import com.github.zjor.telegram.events.SendMessageToCreatorEvent;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.telegram.abilitybots.api.bot.AbilityBot;

/**
 * Subscribes to an event and sends a message via telegram
 */
public class TelegramEventSender {

    private final AbilityBot bot;

    @Inject
    public TelegramEventSender(MqttForwarderBot bot) {
        this.bot = bot;
    }

    @Subscribe
    public void handleMessageEvent(SendMessageEvent e) {
        bot.silent().sendMd(e.getText(), e.getChatId());
    }

    @Subscribe
    public void handleMessageEvent(SendMessageToCreatorEvent e) {
        bot.silent().sendMd(e.getText(), bot.creatorId());
    }

}
