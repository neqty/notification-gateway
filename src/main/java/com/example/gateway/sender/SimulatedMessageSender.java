package com.example.gateway.sender;

import java.util.ArrayList;
import java.util.List;

/** Shared simulation mechanics; no network or external accounts are used. */
public abstract class SimulatedMessageSender implements MessageSender {
    private boolean available = true;
    private final List<SentMessage> deliveries = new ArrayList<>();

    public void setAvailable(boolean available) { this.available = available; }
    @Override public boolean isAvailable() { return available; }
    public List<SentMessage> getDeliveries() { return List.copyOf(deliveries); }

    @Override
    public final boolean sendMessage(String recipient, String title, String body) {
        if (!isAvailable() || recipient == null || recipient.isBlank()
                || title == null || body == null || !accepts(body)) {
            System.out.println("[" + getChannelName() + "] delivery failed");
            return false;
        }
        String payload = format(recipient, title, body);
        deliveries.add(new SentMessage(recipient, title, body, payload));
        System.out.println("[" + getChannelName() + "] " + payload);
        return true;
    }
    protected boolean accepts(String body) { return true; }
    protected abstract String format(String recipient, String title, String body);
}
