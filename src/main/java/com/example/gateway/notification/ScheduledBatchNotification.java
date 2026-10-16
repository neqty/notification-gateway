package com.example.gateway.notification;

import com.example.gateway.sender.MessageSender;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** A queued batch dispatched explicitly by the caller (no background timer). */
public final class ScheduledBatchNotification extends Notification {
    private final List<String> recipients = new ArrayList<>();
    private BatchSummary lastSummary = new BatchSummary(0, 0, 0);
    public ScheduledBatchNotification(MessageSender sender, Collection<String> recipients) {
        super(sender);
        this.recipients.addAll(Objects.requireNonNull(recipients));
    }
    public void addRecipient(String recipient) { recipients.add(recipient); }
    public BatchSummary getLastSummary() { return lastSummary; }
    public BatchSummary sendBatch(String message) { return deliver(recipients, message); }
    @Override public void send(String recipient, String message) {
        // The base-contract recipient is an additional recipient for this dispatch only.
        List<String> targets = new ArrayList<>(recipients);
        if (recipient != null && !recipient.isBlank() && !targets.contains(recipient)) targets.add(recipient);
        deliver(targets, message);
    }
    private BatchSummary deliver(List<String> targets, String message) {
        Objects.requireNonNull(message, "message must not be null");
        int successful = 0;
        for (String target : targets) {
            if (sender.sendMessage(target, "Batch notification", message)) successful++;
        }
        lastSummary = new BatchSummary(targets.size(), successful, targets.size() - successful);
        System.out.println(lastSummary);
        return lastSummary;
    }
}
