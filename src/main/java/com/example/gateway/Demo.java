package com.example.gateway;

import com.example.gateway.notification.*;
import com.example.gateway.sender.*;
import java.util.List;

public final class Demo {
    public static void main(String[] args) {
        TelegramMessageSender telegram = new TelegramMessageSender();
        EmailMessageSender email = new EmailMessageSender();
        SmsMessageSender sms = new SmsMessageSender();
        System.out.println("=== 1. Urgent + Telegram ===");
        UrgentNotification urgent = new UrgentNotification(telegram, sms);
        urgent.send("alice", "Server CPU is above 95%.");
        System.out.println("\n=== 2. Encoded + Email ===");
        new EncryptedNotification(email).send("alice@example.com", "Your one-time code is 4821");
        System.out.println("\n=== 3. Batch + SMS (one invalid recipient) ===");
        new ScheduledBatchNotification(sms, List.of("+77010000001", "", "+77010000002"))
                .sendBatch("Class begins at 10:00.");
        System.out.println("\n=== 4. Same object: Telegram -> Email ===");
        urgent.send("alice", "First transport: Telegram.");
        urgent.setSender(email);
        urgent.send("alice@example.com", "Same object, now Email.");
        System.out.println("\n=== 5. Telegram unavailable -> SMS fallback ===");
        telegram.setAvailable(false);
        urgent.setSender(telegram);
        urgent.send("+77010000001", "Service is down.");
        System.out.println("\n=== 6. Marketing + Push ===");
        new MarketingNotification(new PushNotificationSender()).send("device-42", "20% off today!");
        System.out.println("\n=== 7. Existing notification + Kafka ===");
        urgent.setSender(new KafkaEventSender());
        urgent.send("operations", "Incident recorded.");
        System.out.println("\n=== 8. SMS length failure ===");
        sms.sendMessage("+77010000001", "", "x".repeat(161));
    }
}
