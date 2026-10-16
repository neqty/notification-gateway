package com.example.gateway.sender;

public final class SmsMessageSender extends SimulatedMessageSender {
    public static final int MAX_LENGTH = 160;
    @Override public String getChannelName() { return "SMS"; }
    @Override protected boolean accepts(String body) {
        // Assignment model: Unicode code points, not carrier encoding segments.
        return body.codePointCount(0, body.length()) <= MAX_LENGTH;
    }
    @Override protected String format(String recipient, String title, String body) {
        // SMS has no subject; reject long bodies rather than silently truncate.
        return "to=" + recipient + " | " + body;
    }
}
