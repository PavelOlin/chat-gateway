package com.livechat.gateway.service;

import com.google.gson.Gson;
import com.livechat.gateway.entity.ChatMessage;
import com.livechat.gateway.entity.queue.QueueType;
import com.livechat.gateway.service.queue.IQueueService;
import com.livechat.gateway.service.queue.QueueMessage;
import com.livechat.gateway.storage.ChatMessageStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@DependsOn("liquibase")
public class ChatDataConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatDataConsumer.class);

    private static final int NUM_THREADS_MIN = 0;
    private static final int NUM_THREADS_MAX = 20;
    private static final int BATCH_SIZE_MIN = 1;
    private static final int BATCH_SIZE_MAX = 100;
    private static final int ITERATION_DELAY_MS_MIN = 10;
    private static final int ITERATION_DELAY_MS_MAX = 60 * 1000;
    private static final int SECONDS_TO_LOCK_MIN = 2 * 60;
    private static final int SECONDS_TO_LOCK_MAX = 2 * 60 * 60;

    @Value("${chatDataConsumer.numThreads:2}")
    private int numThreads;

    @Value("${chatDataConsumer.batchSize:10}")
    private int batchSize;

    @Value("${chatDataConsumer.iterationDelayMs:1000}")
    private long iterationDelayMs;

    @Value("${chatDataConsumer.secondsToLock:300}")
    private long secondsToLock;

    private final IQueueService queueService;

    private final ChatMessageStorage chatMessageStorage;

    @Autowired
    public ChatDataConsumer(IQueueService queueService, ChatMessageStorage chatMessageStorage) {
        this.queueService = queueService;
        this.chatMessageStorage = chatMessageStorage;
    }

    @PostConstruct
    void init() {
        validateConfigParams();
        if (numThreads == 0) {
            LOGGER.warn("Consumer was disabled");
            return;
        }

        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(this::consumeLoop);
            thread.setName(ChatDataConsumer.class.getSimpleName() + "-" + i);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void validateConfigParams() {
        validateConfigParamRange(numThreads,  "numThreads", NUM_THREADS_MIN, NUM_THREADS_MAX);
        validateConfigParamRange(batchSize,  "batchSize", BATCH_SIZE_MIN, BATCH_SIZE_MAX);
        validateConfigParamRange(iterationDelayMs,  "iterationDelayMs", ITERATION_DELAY_MS_MIN, ITERATION_DELAY_MS_MAX);
        validateConfigParamRange(secondsToLock,  "secondsToLock", SECONDS_TO_LOCK_MIN, SECONDS_TO_LOCK_MAX);
    }

    private void validateConfigParamRange(long actualValue, String name, long minValue, long maxValue) {
        if (actualValue < minValue || actualValue > maxValue) {
            throw new RuntimeException(String.format("Config property 'chatDataConsumer.%s' should be from %d to %d", name, minValue, maxValue));
        }
    }

    private void consumeLoop() {
        try {
            while (true) {
                int consumed = consume();
                if (consumed == 0) {
                    Thread.sleep(iterationDelayMs);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Thread was interrupted");
        } catch (Exception e) {
            LOGGER.error("Got unhandled exception during consume chat data from queue", e);
        }
    }

    private int consume() {
        long startTime = System.currentTimeMillis();
        LOGGER.debug("Consuming records from queue...");
        List<QueueMessage> messages = queueService.receiveMessages(QueueType.CHAT_POST_MESSAGE, secondsToLock, batchSize);
        if (messages.isEmpty()) {
            return 0;
        }
        List<Long> ids = new ArrayList<>(messages.size());
        List<ChatMessage> messagesToSave = new ArrayList<>(messages.size());
        Gson gson = new Gson();
        for (QueueMessage qm: messages) {
            ids.add(qm.getId());
            ChatMessage cm = deserialize(gson, qm);
            messagesToSave.add(cm);
        }

        chatMessageStorage.save(messagesToSave);

        queueService.deleteMessages(ids);
        LOGGER.debug("Consumed {} messages in {} ms", messages.size(), System.currentTimeMillis() - startTime);
        return messages.size();
    }

    private static ChatMessage deserialize(Gson gson, QueueMessage queueMessage) {
        return gson.fromJson(queueMessage.getMessage(), ChatMessage.class);
    }
}
