package com.example.gateway.sender;

public final class TelegramMessageSender extends SimulatedMessageSender {
    @Override public String getChannelName() { return "Telegram"; }
    @Override protected String format(String recipient, String title, String body) {
        return "chat=" + recipient + " | **" + title + "** | " + body;
    }
}
