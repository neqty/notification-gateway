package com.example.gateway.sender;

public final class KafkaEventSender extends SimulatedMessageSender {
    @Override public String getChannelName() { return "Kafka"; }
    @Override protected String format(String recipient, String title, String body) {
        return "topic=notifications | {\"recipient\":\"" + escape(recipient)
                + "\",\"title\":\"" + escape(title) + "\",\"body\":\"" + escape(body) + "\"}";
    }
    private static String escape(String value) {
        StringBuilder result = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (c == '"' || c == '\\') result.append('\\').append(c);
            else if (c < 0x20) result.append(String.format("\\u%04x", (int) c));
            else result.append(c);
        }
        return result.toString();
    }
}
