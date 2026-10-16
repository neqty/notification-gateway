package com.example.gateway.sender;

/** Implementor: the transport contract used by every notification. */
public interface MessageSender {
    boolean sendMessage(String recipient, String title, String body);
    boolean isAvailable();
    String getChannelName();
}
