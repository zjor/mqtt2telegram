package com.github.zjor.telegram.events;

import lombok.Data;

@Data
public class SendMessageEvent {
    private final long chatId;
    private final String text;
}
