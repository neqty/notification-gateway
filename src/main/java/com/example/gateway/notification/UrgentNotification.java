package com.example.gateway.notification;

import com.example.gateway.sender.MessageSender;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class UrgentNotification extends Notification {
    private final List<MessageSender> fallbacks = new ArrayList<>();
    private boolean lastDeliverySuccessful;

    public UrgentNotification(MessageSender sender, MessageSender... fallbacks) {
        super(sender);
        for (MessageSender fallback : fallbacks) addFallback(fallback);
    }
    public void addFallback(MessageSender fallback) {
        fallbacks.add(Objects.requireNonNull(fallback));
    }
    public boolean wasLastDeliverySuccessful() { return lastDeliverySuccessful; }
    @Override public void send(String recipient, String message) {
        Objects.requireNonNull(message, "message must not be null");
        String body = "[URGENT] " + message;
        lastDeliverySuccessful = deliver(sender, recipient, body);
        if (!lastDeliverySuccessful) {
            for (MessageSender fallback : fallbacks) {
                if (fallback == sender) continue;
                System.out.println("Trying fallback: " + fallback.getChannelName());
                if (deliver(fallback, recipient, body)) {
                    lastDeliverySuccessful = true;
                    break;
                }
            }
        }
        if (!lastDeliverySuccessful) System.out.println("Urgent notification failed on all providers.");
    }
    private boolean deliver(MessageSender candidate, String recipient, String body) {
        return candidate.isAvailable() && candidate.sendMessage(recipient, "Urgent notification", body);
    }
}
