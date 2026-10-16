package com.example.gateway.notification;

import com.example.gateway.sender.MessageSender;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/** Educational transformation only: Base64 is reversible encoding, not encryption. */
public final class EncryptedNotification extends Notification {
    public EncryptedNotification(MessageSender sender) { super(sender); }
    @Override public void send(String recipient, String message) {
        Objects.requireNonNull(message, "message must not be null");
        String encoded = Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
        if (!sender.sendMessage(recipient, "Encoded notification", encoded))
            System.out.println("Encoded notification failed.");
    }
}
