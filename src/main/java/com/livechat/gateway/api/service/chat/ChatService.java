package com.livechat.gateway.api.service.chat;

import com.google.gson.Gson;
import com.livechat.gateway.api.controller.dto.ChatMessage;
import com.livechat.gateway.api.controller.dto.PostMessageRequest;
import com.livechat.gateway.api.entity.chat.ConversationMessage;
import com.livechat.gateway.api.service.queue.IQueueService;
import com.livechat.gateway.api.storage.chat.ConversationMessageStorage;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final int PAYLOAD_MAX_SIZE = 256;

    private final IQueueService queueService;
    private final ConversationMessageStorage conversationMessageStorage;

    @Autowired
    public ChatService(IQueueService queueService, ConversationMessageStorage conversationMessageStorage) {
        this.queueService = queueService;
        this.conversationMessageStorage = conversationMessageStorage;
    }

    public void postMessage(long userId, long conversationId, PostMessageRequest request, String clientIp) {
        long timestamp = System.currentTimeMillis();
        validateInputParams(request, clientIp);
        ConversationMessage convMessage = new ConversationMessage(userId, conversationId, clientIp, timestamp, request.getMessage());
        Gson gson = new Gson();
        String payload = gson.toJson(convMessage);
        if (payload.length() > PAYLOAD_MAX_SIZE) {
            throw new RuntimeException("Unexpected payload size " + payload.length() + ", expected up to " + PAYLOAD_MAX_SIZE);
        }
        queueService.sendMessage(payload);
    }

    public List<ChatMessage> readMessages(long userId, long conversationId, long fromMessageId, long count) {
        List<ConversationMessage> messages = conversationMessageStorage.readMessages(userId, conversationId, fromMessageId, count);
        return messages.stream().map(this::transformMessage).collect(Collectors.toList());
    }

    private ChatMessage transformMessage(ConversationMessage conversationMessage) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(conversationMessage.getId());
        chatMessage.setConversationId(conversationMessage.getConversationId());
        chatMessage.setUserId(conversationMessage.getUserId());
        chatMessage.setClientIp(conversationMessage.getClientIp());
        chatMessage.setTimestamp(conversationMessage.getTimestamp());
        chatMessage.setEncodedMessage(conversationMessage.getEncodedMessage());

        // and probably more fields joined from User table
        return chatMessage;
    }

    private static void validateInputParams(PostMessageRequest req, String clientIp) {
        if (req == null) {
            throw new IllegalArgumentException("missing request body");
        }

        String message = req.getMessage();
        if (message == null || message.isEmpty() || message.length() > 100) {
            throw new IllegalArgumentException("'message' param should has size from 1 to 100 characters");
        }

        if (clientIp != null && (clientIp.length() < 7 || clientIp.length() > 16)) {
            throw new IllegalArgumentException("client IP address has incorrect value [" + clientIp + "]");
        }
    }

}
