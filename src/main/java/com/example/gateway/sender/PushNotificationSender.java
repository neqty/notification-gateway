package com.example.gateway.sender;

/** Independent extension: no existing notification needs modification. */
public final class PushNotificationSender extends SimulatedMessageSender {
    @Override public String getChannelName() { return "Push"; }
    @Override protected String format(String recipient, String title, String body) {
        return "device=" + recipient + " | banner=[" + title + "] " + body;
    }
}
