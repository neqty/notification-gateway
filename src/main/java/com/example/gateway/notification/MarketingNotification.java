package com.example.gateway.notification;

import com.example.gateway.sender.MessageSender;
import java.util.Objects;

/** Independent extension: works through the existing MessageSender interface. */
public final class MarketingNotification extends Notification {
    public MarketingNotification(MessageSender sender) { super(sender); }
    @Override public void send(String recipient, String message) {
        Objects.requireNonNull(message, "message must not be null");
        String body = "[PROMO] " + message + " | Reply STOP to unsubscribe.";
        if (!sender.sendMessage(recipient, "Special offer", body))
            System.out.println("Marketing notification failed.");
    }
}
