package com.livechat.gateway.service;

import com.google.gson.Gson;
import com.livechat.gateway.entity.ChatMessage;
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

    private static final Logger logger = LoggerFactory.getLogger(ChatDataConsumer.class);

    @Value("${dataConsumer.numThreads:2}")
    private int numThreads;

    @Value("${dataConsumer.batchSize:10}")
    private int batchSize;

    @Value("${dataConsumer.iterationDelayMs:1000}")
    private long iterationDelayMs;

    private final IQueueService queueService;

    private final ChatMessageStorage chatMessageStorage;

    @Autowired
    public ChatDataConsumer(IQueueService queueService, ChatMessageStorage chatMessageStorage) {
        this.queueService = queueService;
        this.chatMessageStorage = chatMessageStorage;
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
        List<ChatMessage> messagesToSave = new ArrayList<>(messages.size());
        Gson gson = new Gson();
        for (QueueMessage qm: messages) {
            ids.add(qm.getId());
            ChatMessage cm = deserialize(gson, qm);
            messagesToSave.add(cm);
        }

        chatMessageStorage.save(messagesToSave);

        queueService.deleteMessages(ids);
        logger.debug("Consumed {} messages in {} ms", messages.size(), System.currentTimeMillis() - startTime);
        return messages.size();
    }

    private static ChatMessage deserialize(Gson gson, QueueMessage queueMessage) {
        return gson.fromJson(queueMessage.getMessage(), ChatMessage.class);
    }
}
