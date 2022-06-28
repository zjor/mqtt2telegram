package com.github.zjor.telegram.events;

import lombok.Data;

@Data
public class SendMessageToCreatorEvent {
    private final String text;
}
