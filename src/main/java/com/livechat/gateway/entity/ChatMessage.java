package com.livechat.gateway.entity;

public class ChatMessage {
    private long id;
    private long chatId;
    private long userId;
    private String clientIp;
    private long timestamp;
    private String encodedMessage;

    public ChatMessage() {}

    public ChatMessage(long userId, long chatId, String clientIp, long timestamp, String encodedMessage) {
        this(0, userId, chatId, clientIp, timestamp, encodedMessage);
    }

    public ChatMessage(long id, long userId, long chatId, String clientIp, long timestamp, String encodedMessage) {
        this.id = id;
        this.userId = userId;
        this.chatId = chatId;
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

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}
