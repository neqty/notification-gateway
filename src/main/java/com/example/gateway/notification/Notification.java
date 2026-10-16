package com.example.gateway.notification;

import com.example.gateway.sender.MessageSender;
import java.util.Objects;

/** Abstraction. The sender field is the Bridge (composition). */
public abstract class Notification {
    protected MessageSender sender;
    protected Notification(MessageSender sender) { setSender(sender); }
    public void setSender(MessageSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender must not be null");
    }
    public abstract void send(String recipient, String message);
}
