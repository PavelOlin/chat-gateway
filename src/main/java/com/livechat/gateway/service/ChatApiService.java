package com.livechat.gateway.service;

import com.google.gson.Gson;
import com.livechat.gateway.dto.ChatMessageDto;
import com.livechat.gateway.dto.PostMessageRequest;
import com.livechat.gateway.entity.ChatMessage;
import com.livechat.gateway.service.queue.IQueueService;
import com.livechat.gateway.storage.ChatMessageStorage;
import com.livechat.gateway.transformer.ChatMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatApiService {

    private static final Logger logger = LoggerFactory.getLogger(ChatApiService.class);
    private static final int PAYLOAD_MAX_SIZE = 256;
    private static final int CLIENT_IP_MIN_SIZE = 7; // IPv4: 0.0.0.0
    private static final int CLIENT_IP_MAX_SIZE = 45; // IPv6 max size
    private static final int MESSAGE_MIN_SIZE = 1;
    private static final int MESSAGE_MAX_SIZE = 100;

    private final IQueueService queueService;
    private final ChatMessageStorage chatMessageStorage;
    private final ChatMessageTransformer chatMessageTransformer;

    @Autowired
    public ChatApiService(IQueueService queueService, ChatMessageStorage chatMessageStorage, ChatMessageTransformer chatMessageTransformer) {
        this.queueService = queueService;
        this.chatMessageStorage = chatMessageStorage;
        this.chatMessageTransformer = chatMessageTransformer;
    }

    public void postMessage(long userId, long chatId, PostMessageRequest request, String clientIp) {
        long timestamp = System.currentTimeMillis();
        validateInputParams(request, clientIp);
        ChatMessage chatMessage = new ChatMessage(userId, chatId, clientIp, timestamp, request.getMessage());
        Gson gson = new Gson();
        String payload = gson.toJson(chatMessage);
        if (payload.length() > PAYLOAD_MAX_SIZE) {
            throw new RuntimeException("Unexpected payload size " + payload.length() + ", expected up to " + PAYLOAD_MAX_SIZE);
        }
        logger.debug("Post message: {}", payload);
        queueService.sendMessage(payload);
    }

    public List<ChatMessageDto> readMessages(long userId, long conversationId, long fromMessageId, long count) {
        List<ChatMessage> messages = chatMessageStorage.readMessages(conversationId, fromMessageId, count);
        return messages.stream().map(chatMessageTransformer::toDto).collect(Collectors.toList());
    }

    private static void validateInputParams(PostMessageRequest req, String clientIp) {
        if (req == null) {
            throw new IllegalArgumentException("Missing request body");
        }

        String message = req.getMessage();
        if (message == null || message.isEmpty() || message.length() > MESSAGE_MAX_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Message param should has size from %d to %d characters", MESSAGE_MIN_SIZE, MESSAGE_MAX_SIZE));
        }

        // IPv4/IPv6
        if (clientIp != null && (clientIp.length() < CLIENT_IP_MIN_SIZE || clientIp.length() > CLIENT_IP_MAX_SIZE)) {
            throw new IllegalArgumentException("Client IP address has incorrect value [" + clientIp + "]");
        }
    }

}
