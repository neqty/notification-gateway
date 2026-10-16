package com.example.gateway.sender;

public final class EmailMessageSender extends SimulatedMessageSender {
    @Override public String getChannelName() { return "Email"; }
    @Override protected String format(String recipient, String title, String body) {
        return "To: " + recipient + "\nSubject: " + title + "\n" + body
                + "\n--\nNotification Gateway Team";
    }
}
