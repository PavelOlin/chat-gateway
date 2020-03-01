package com.livechat.gateway.service.queue;

import java.util.Collections;
import java.util.List;

public interface IQueueService {
    void sendMessages(List<String> messages);
    void deleteMessages(List<Long> messageIds);
    List<QueueMessage> receiveMessages(int count);

    default void sendMessage(String message) {
        sendMessages(Collections.singletonList(message));
    }
    default void deleteMessage(long messageId) {
        deleteMessages(Collections.singletonList(messageId));
    }
    default QueueMessage receiveMessage() {
        return receiveMessages(1).get(0);
    }
}
