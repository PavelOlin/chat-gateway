package com.livechat.gateway.consumer.service;

import com.google.gson.Gson;
import com.livechat.gateway.api.entity.chat.ConversationMessage;
import com.livechat.gateway.api.service.queue.IQueueService;
import com.livechat.gateway.api.service.queue.QueueMessage;
import com.livechat.gateway.api.storage.chat.ConversationMessageStorage;
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

    private static final Logger logger = LoggerFactory.getLogger(ChatDataConsumer.class);

    @Value("${dataConsumer.numThreads:2}")
    private int numThreads;

    @Value("${dataConsumer.batchSize:10}")
    private int batchSize;

    @Value("${dataConsumer.iterationDelayMs:1000}")
    private long iterationDelayMs;

    private final IQueueService queueService;

    private final ConversationMessageStorage conversationMessageStorage;

    @Autowired
    public ChatDataConsumer(IQueueService queueService, ConversationMessageStorage conversationMessageStorage) {
        this.queueService = queueService;
        this.conversationMessageStorage = conversationMessageStorage;
    }

    @PostConstruct
    void init() {
        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(this::consumeLoop);
            thread.setName("DataConsumer-" + i);
            thread.setDaemon(true);
            thread.start();
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
            logger.warn("Thread was interrupted");
        } catch (Exception e) {
            logger.error("Got unhandled exception during consume chat data from queue", e);
        }
    }

    private int consume() {
        long startTime = System.currentTimeMillis();
        logger.debug("Consuming records from queue...");
        List<QueueMessage> messages = queueService.receiveMessages(batchSize);
        if (messages.isEmpty()) {
            return 0;
        }
        List<Long> ids = new ArrayList<>(messages.size());
        List<ConversationMessage> messagesToSave = new ArrayList<>(messages.size());
        Gson gson = new Gson();
        for (QueueMessage qm: messages) {
            ids.add(qm.getId());
            ConversationMessage cm = transform(gson, qm);
            messagesToSave.add(cm);
        }

        conversationMessageStorage.save(messagesToSave);

        queueService.deleteMessages(ids);
        logger.debug("Consumed {} messages in {} ms", messages.size(), System.currentTimeMillis() - startTime);
        return messages.size();
    }

    private static ConversationMessage transform(Gson gson, QueueMessage queueMessage) {
        return gson.fromJson(queueMessage.getMessage(), ConversationMessage.class);
    }
}
