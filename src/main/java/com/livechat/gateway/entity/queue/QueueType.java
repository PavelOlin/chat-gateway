package com.livechat.gateway.entity.queue;

public enum QueueType {
    CHAT_POST_MESSAGE(1);

    private final int value;

    QueueType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static QueueType getByValue(int value) {
        for (QueueType type: values()) {
            if (type.getValue() == value) {
                return type;
            }
        }

        throw new RuntimeException("Unable to get QueueType by value=" + value);
    }
}
