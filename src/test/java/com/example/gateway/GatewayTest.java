package com.example.gateway;

import com.example.gateway.notification.*;
import com.example.gateway.sender.*;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GatewayTest {
    @Test void telegramDeliversFormattedMessage() {
        var sender = new TelegramMessageSender();
        assertTrue(sender.sendMessage("alice", "Title", "Body"));
        assertEquals("chat=alice | **Title** | Body", sender.getDeliveries().get(0).payload());
    }
    @Test void emailIncludesSubjectAndSignature() {
        var sender = new EmailMessageSender();
        assertTrue(sender.sendMessage("a@example.com", "Report", "Done"));
        var payload = sender.getDeliveries().get(0).payload();
        assertTrue(payload.contains("Subject: Report"));
        assertTrue(payload.endsWith("Notification Gateway Team"));
    }
    @Test void unavailableProviderDoesNotRecordDelivery() {
        var sender = new TelegramMessageSender();
        sender.setAvailable(false);
        assertFalse(sender.sendMessage("alice", "Title", "Body"));
        assertTrue(sender.getDeliveries().isEmpty());
    }
    @Test void urgentAddsPrefix() {
        var sender = new TelegramMessageSender();
        var notification = new UrgentNotification(sender);
        notification.send("alice", "Alert");
        assertEquals("[URGENT] Alert", sender.getDeliveries().get(0).body());
        assertTrue(notification.wasLastDeliverySuccessful());
    }
    @Test void unavailablePrimaryFallsBackToSms() {
        var primary = new TelegramMessageSender();
        primary.setAvailable(false);
        var backup = new SmsMessageSender();
        var notification = new UrgentNotification(primary, backup);
        notification.send("+77010000001", "Alert");
        assertTrue(primary.getDeliveries().isEmpty());
        assertEquals(1, backup.getDeliveries().size());
        assertTrue(notification.wasLastDeliverySuccessful());
    }
    @Test void successfulPrimaryDoesNotUseFallback() {
        var primary = new TelegramMessageSender();
        var backup = new SmsMessageSender();
        new UrgentNotification(primary, backup).send("alice", "Alert");
        assertEquals(1, primary.getDeliveries().size());
        assertTrue(backup.getDeliveries().isEmpty());
    }
    @Test void fallbackAlsoHandlesRejectedDelivery() {
        var primary = new SmsMessageSender();
        var backup = new EmailMessageSender();
        var notification = new UrgentNotification(primary, backup);
        notification.send("alice", "x".repeat(161));
        assertTrue(primary.getDeliveries().isEmpty());
        assertEquals(1, backup.getDeliveries().size());
        assertTrue(notification.wasLastDeliverySuccessful());
    }
    @Test void fallbackChainSkipsUnavailableProviders() {
        var first = new TelegramMessageSender(); first.setAvailable(false);
        var second = new EmailMessageSender(); second.setAvailable(false);
        var third = new PushNotificationSender();
        var notification = new UrgentNotification(first, second);
        notification.addFallback(third);
        notification.send("alice", "Alert");
        assertEquals(1, third.getDeliveries().size());
        assertTrue(notification.wasLastDeliverySuccessful());
    }
    @Test void allUnavailableReportsFailureAndResetsPreviousStatus() {
        var first = new TelegramMessageSender();
        var second = new SmsMessageSender();
        var notification = new UrgentNotification(first, second);
        notification.send("alice", "First");
        assertTrue(notification.wasLastDeliverySuccessful());
        first.setAvailable(false); second.setAvailable(false);
        notification.send("alice", "Second");
        assertFalse(notification.wasLastDeliverySuccessful());
        assertEquals(1, first.getDeliveries().size());
        assertTrue(second.getDeliveries().isEmpty());
    }
    @Test void smsAcceptsExactly160Characters() {
        assertTrue(new SmsMessageSender().sendMessage("alice", "", "x".repeat(160)));
    }
    @Test void smsRejects161CharactersWithoutTruncation() {
        var sender = new SmsMessageSender();
        assertFalse(sender.sendMessage("alice", "", "x".repeat(161)));
        assertTrue(sender.getDeliveries().isEmpty());
    }
    @Test void smsCountsUnicodeCodePoints() {
        var sender = new SmsMessageSender();
        assertTrue(sender.sendMessage("alice", "", "😀".repeat(160)));
        assertFalse(sender.sendMessage("alice", "", "😀".repeat(161)));
    }
    @Test void encryptedNotificationTransformsUtf8BeforeSending() {
        var sender = new EmailMessageSender();
        String original = "Секрет 123";
        new EncryptedNotification(sender).send("alice", original);
        String sent = sender.getDeliveries().get(0).body();
        assertNotEquals(original, sent);
        assertEquals(original, new String(Base64.getDecoder().decode(sent), StandardCharsets.UTF_8));
    }
    @Test void batchCountsPartialFailure() {
        var sender = new SmsMessageSender();
        var batch = new ScheduledBatchNotification(sender, List.of("alice", "", "bob"));
        assertEquals(new BatchSummary(3, 2, 1), batch.sendBatch("Hello"));
        assertEquals(2, sender.getDeliveries().size());
        assertEquals("Total: 3\nSuccessful: 2\nFailed: 1", batch.getLastSummary().toString());
    }
    @Test void emptyBatchIsValid() {
        var batch = new ScheduledBatchNotification(new SmsMessageSender(), List.of());
        assertEquals(new BatchSummary(0, 0, 0), batch.sendBatch("Hello"));
    }
    @Test void unavailableBatchCountsAllFailures() {
        var sender = new SmsMessageSender(); sender.setAvailable(false);
        var batch = new ScheduledBatchNotification(sender, List.of("alice", "bob"));
        assertEquals(new BatchSummary(2, 0, 2), batch.sendBatch("Hello"));
    }
    @Test void batchBaseMethodIncludesAdditionalRecipientOnlyForThisDispatch() {
        var batch = new ScheduledBatchNotification(new SmsMessageSender(), List.of("alice"));
        batch.send("bob", "Hello");
        assertEquals(new BatchSummary(2, 2, 0), batch.getLastSummary());
        assertEquals(new BatchSummary(1, 1, 0), batch.sendBatch("Again"));
    }
    @Test void runtimeSwitchUsesSameNotificationObject() {
        var telegram = new TelegramMessageSender(); var email = new EmailMessageSender();
        Notification notification = new UrgentNotification(telegram);
        notification.send("alice", "First");
        notification.setSender(email);
        notification.send("alice", "Second");
        assertEquals(1, telegram.getDeliveries().size());
        assertEquals("[URGENT] First", telegram.getDeliveries().get(0).body());
        assertEquals("[URGENT] Second", email.getDeliveries().get(0).body());
    }
    @Test void pushWorksWithExistingEncryptedNotification() {
        var push = new PushNotificationSender();
        new EncryptedNotification(push).send("device-42", "Hello");
        assertEquals("SGVsbG8=", push.getDeliveries().get(0).body());
        assertTrue(push.getDeliveries().get(0).payload().startsWith("device=device-42 | banner="));
    }
    @Test void marketingWorksWithBothOldEmailAndNewPush() {
        var email = new EmailMessageSender(); var push = new PushNotificationSender();
        var marketing = new MarketingNotification(email);
        marketing.send("alice", "Sale");
        marketing.setSender(push); marketing.send("device-42", "Sale");
        for (var sender : List.of(email, push)) {
            assertEquals("Special offer", sender.getDeliveries().get(0).title());
            assertEquals("[PROMO] Sale | Reply STOP to unsubscribe.", sender.getDeliveries().get(0).body());
        }
    }
    @Test void kafkaPublishesEscapedEvent() {
        var sender = new KafkaEventSender();
        assertTrue(sender.sendMessage("ops", "Alert", "A\"B\nC\\D"));
        assertEquals("topic=notifications | {\"recipient\":\"ops\",\"title\":\"Alert\",\"body\":\"A\\\"B\\u000aC\\\\D\"}",
                sender.getDeliveries().get(0).payload());
    }
    @Test void invalidRecipientAndNullBodyFailSafely() {
        var sender = new EmailMessageSender();
        assertFalse(sender.sendMessage("  ", "Title", "Body"));
        assertFalse(sender.sendMessage(null, "Title", "Body"));
        assertFalse(sender.sendMessage("alice", "Title", null));
        assertTrue(sender.getDeliveries().isEmpty());
    }
    @Test void nullSenderRejectedWithoutLosingPreviousSender() {
        var sender = new EmailMessageSender();
        var notification = new UrgentNotification(sender);
        assertThrows(NullPointerException.class, () -> notification.setSender(null));
        notification.send("alice", "Still works");
        assertEquals(1, sender.getDeliveries().size());
    }
    @Test void deliveryHistoryCannotBeModifiedByCaller() {
        var sender = new SmsMessageSender(); sender.sendMessage("alice", "", "Hello");
        assertThrows(UnsupportedOperationException.class, () -> sender.getDeliveries().clear());
        assertEquals(1, sender.getDeliveries().size());
    }
    @Test void everyNotificationWorksWithEveryProvider() {
        for (var sender : List.of(new TelegramMessageSender(), new EmailMessageSender(),
                new SmsMessageSender(), new KafkaEventSender(), new PushNotificationSender())) {
            new UrgentNotification(sender).send("alice", "Alert");
            new EncryptedNotification(sender).send("alice", "Secret");
            new ScheduledBatchNotification(sender, List.of("alice")).sendBatch("Hello");
            new MarketingNotification(sender).send("alice", "Sale");
            assertEquals(4, sender.getDeliveries().size(), sender.getChannelName());
        }
    }
}
