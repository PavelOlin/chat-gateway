package com.livechat.gateway.service.queue;

import com.livechat.gateway.entity.queue.QueueType;

import java.util.Collections;
import java.util.List;

public interface IQueueService {
    void sendMessages(QueueType queueType, List<String> messages);
    void deleteMessages(List<Long> messageIds);
    List<QueueMessage> receiveMessages(QueueType queueType, long secondsToLock, int count);

    default void sendMessage(QueueType queueType, String message) {
        sendMessages(queueType, Collections.singletonList(message));
    }
    default void deleteMessage(long messageId) {
        deleteMessages(Collections.singletonList(messageId));
    }
    default QueueMessage receiveMessage(QueueType queueType, long secondsToLock) {
        return receiveMessages(queueType, secondsToLock,1).get(0);
    }
}
