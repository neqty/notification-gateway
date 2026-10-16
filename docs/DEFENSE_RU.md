# Подготовка к защите

## Объяснение за одну минуту

В проекте есть две независимые оси: тип уведомления и канал доставки. `Notification` — абстракция, а `MessageSender` — интерфейс реализации. Мост находится в поле `protected MessageSender sender`: уведомление хранит ссылку на отправитель и делегирует ему доставку.

`UrgentNotification` добавляет `[URGENT]`, `EncryptedNotification` кодирует текст в Base64, `ScheduledBatchNotification` обходит получателей и считает результаты, `MarketingNotification` добавляет рекламный текст. Telegram, Email, SMS, Kafka и Push по-разному формируют отправку. Все отправки имитируются в консоли.

Поэтому не нужны классы `UrgentTelegramNotification` для каждой комбинации. Новый канал реализует `MessageSender`, новый тип наследует `Notification`. В `setSender` можно заменить канал у уже существующего объекта. Fallback тоже работает через интерфейс, без проверок конкретных типов.

## Что открыть на защите

1. `README.md`: UML и ответы на вопросы задания.
2. `Notification.java`: поле `sender` и метод `setSender` — место моста.
3. `MessageSender.java`: контракт канала доставки.
4. `UrgentNotification.java`: добавление префикса, проверка доступности и цепочка fallback.
5. `Demo.java`: восемь пронумерованных сценариев, включая все шесть обязательных.
6. `GatewayTest.java`: 25 тестов, включая матрицу всех 20 комбинаций.

## Команды

```sh
java -jar dist/notification-gateway-1.0.0.jar
mvn test
mvn package
java -jar target/notification-gateway-1.0.0.jar
```

После изменения исходников запускай сборку и JAR из `target`, поскольку JAR в `dist` — заранее собранная версия.

## Если попросят изменить требование

### Уменьшить SMS до 120 символов

В `SmsMessageSender.java` поменять `MAX_LENGTH = 160` на `120`. Обновить граничные тесты: 120 проходит, 121 не проходит. Сейчас префикс `[URGENT] ` входит в длину SMS: это 9 символов, поэтому исходный срочный текст ограничен 151 символом при лимите 160.

### Сделать Email недоступным и добавить резервный канал

В `Demo.main`:

```java
email.setAvailable(false);
UrgentNotification example = new UrgentNotification(email, sms);
example.send("+77010000001", "Check fallback");
```

### Добавить ещё один fallback

```java
urgent.addFallback(new PushNotificationSender());
```

Каналы проверяются последовательно. При первом успехе цикл заканчивается. Если все каналы недоступны или отклоняют сообщение, `wasLastDeliverySuccessful()` вернёт `false`.

### Заменить канал во время работы

```java
urgent.setSender(new KafkaEventSender());
urgent.send("operations", "Now using Kafka");
```

Создавать новое уведомление не нужно.

### Добавить WhatsAppMessageSender

Создать файл в пакете `com.example.gateway.sender`:

```java
package com.example.gateway.sender;

public final class WhatsAppMessageSender extends SimulatedMessageSender {
    @Override public String getChannelName() { return "WhatsApp"; }
    @Override protected String format(String recipient, String title, String body) {
        return "WhatsApp to=" + recipient + " | " + title + " | " + body;
    }
}
```

Затем `urgent.setSender(new WhatsAppMessageSender());`. Старые классы уведомлений не меняются.

### Добавить SilentNotification

Создать файл в пакете `com.example.gateway.notification`:

```java
package com.example.gateway.notification;

import com.example.gateway.sender.MessageSender;

public final class SilentNotification extends Notification {
    public SilentNotification(MessageSender sender) { super(sender); }
    @Override public void send(String recipient, String message) {
        sender.sendMessage(recipient, "Silent notification", "[SILENT] " + message);
    }
}
```

Это учебная метка тихого уведомления. Настоящее отключение звука потребовало бы поддержки опции в API канала. Нынешний контракт передаёт только получателя, заголовок и текст.

## Частые вопросы

**Почему Bridge, а не Adapter?** Bridge разделяет две изначально проектируемые иерархии. Adapter согласует несовместимый существующий интерфейс с ожидаемым. Здесь несовместимого старого API нет.

**Почему Bridge иногда похож на Strategy?** Оба используют композицию и заменяемую реализацию. Здесь цель — независимое расширение двух иерархий: поведения уведомлений и транспорта. Strategy обычно выделяет заменяемый алгоритм внутри одного контекста.

**Base64 действительно шифрует?** Нет. Это обратимое кодирование без секретного ключа. В задании Base64 явно разрешён как учебное преобразование.

**Где планировщик у ScheduledBatchNotification?** По обязательным требованиям нужны коллекция адресатов, массовая отправка и итоговые счётчики. Здесь момент запуска задаёт вызывающий код через `sendBatch`. Фонового таймера нет.

**Что значит успешная отправка?** Симулятор принял данные, сформировал вывод и добавил запись в историю. Реальных сетевых запросов и подтверждений доставки нет.

**Что произойдёт с длинным закодированным SMS?** Base64 увеличивает длину. Ограничение проверяется после преобразования; SMS вернёт `false`, если результат длиннее лимита.

**Что демонстрирует независимость расширений?** `PushNotificationSender` работает со старым `EncryptedNotification`, а `MarketingNotification` работает со старым `EmailMessageSender`. Тесты проверяют оба случая без изменения этих старых классов.

## Перед сдачей

Распакуй архив, запусти демо, загрузи содержимое папки в GitHub и отправь ссылку в Moodle. Прочитай этот файл и разберись с `Notification`, `MessageSender`, `UrgentNotification`: на индивидуальной защите понадобится объяснить код и внести изменение.
