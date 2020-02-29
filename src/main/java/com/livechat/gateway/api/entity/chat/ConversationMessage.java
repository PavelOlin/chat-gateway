package com.livechat.gateway.api.entity.chat;

public class ConversationMessage {
    private long id;
    private long conversationId;
    private long userId;
    private String clientIp;
    private long timestamp;
    private String encodedMessage;

    public ConversationMessage() {}

    public ConversationMessage(long userId, long conversationId, String clientIp, long timestamp, String encodedMessage) {
        this(0, userId, conversationId, clientIp, timestamp, encodedMessage);
    }

    public ConversationMessage(long id, long userId, long conversationId, String clientIp, long timestamp, String encodedMessage) {
        this.id = id;
        this.userId = userId;
        this.conversationId = conversationId;
        this.clientIp = clientIp;
        this.timestamp = timestamp;
        this.encodedMessage = encodedMessage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEncodedMessage() {
        return encodedMessage;
    }

    public void setEncodedMessage(String encodedMessage) {
        this.encodedMessage = encodedMessage;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }
}
